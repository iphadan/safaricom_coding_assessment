package com.safaricom.fake_bank.service;

import com.safaricom.fake_bank.dto.BankPayoutRecord;
import com.safaricom.fake_bank.dto.PayoutRequest;
import com.safaricom.fake_bank.dto.PayoutResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BankService {

    private final Map<String, BankPayoutRecord> payoutStore = new ConcurrentHashMap<>();
    private final AtomicInteger totalPaidSantim = new AtomicInteger(0);
    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${bank.simulation.success-rate:0.80}")
    private double successRate;

    @Value("${bank.simulation.rejection-rate:0.08}")
    private double rejectionRate;

    @Value("${bank.simulation.timeout-rate:0.12}")
    private double timeoutRate;

    @Value("${bank.simulation.callback-failure-rate:0.05}")
    private double callbackFailureRate;

    @Value("${bank.simulation.duplicate-callback-rate:0.30}")
    private double duplicateCallbackRate;

    public PayoutResponse processPayout(PayoutRequest req) {
        // 1. Idempotency Check on payout_id
        if (payoutStore.containsKey(req.getPayoutId())) {
            BankPayoutRecord existing = payoutStore.get(req.getPayoutId());
            // A known ID with a different amount is rejected (per spec 4.1)
            if (existing.getAmount() != req.getAmount()) {
                return new PayoutResponse("REJECTED", null, "AMOUNT_MISMATCH");
            }
            // Return original result
            return new PayoutResponse(existing.getResult(), existing.getBankRef(), existing.getResult().equals("REJECTED") ? "BENEFICIARY_ACCOUNT_INVALID" : null);
        }

        // 2. Simulate 2 to 6 seconds blocking latency
        int latencySeconds = 2 + random.nextInt(5);
        sleep(latencySeconds * 1000L);

        // 3. Determine outcome based on weights
        double roll = random.nextDouble();
        String outcome;
        if (roll < successRate) {
            outcome = "ACCEPTED";
        } else if (roll < successRate + rejectionRate) {
            outcome = "REJECTED";
        } else {
            // Timeout / No Response simulation (stays open, returns 504)
            // Spec: "In half of those the bank did process the payout and a callback follows; in the other half it never received it."
            boolean processedDespiteTimeout = random.nextBoolean();
            if (processedDespiteTimeout) {
                registerAcceptedPayout(req, true); // Processed internally, will send callback later
            }
            sleep(5000); // Simulate hang before 504
            throw new RuntimeException("Gateway Timeout 504");
        }

        if (outcome.equals("REJECTED")) {
            BankPayoutRecord record = new BankPayoutRecord();
            record.setPayoutId(req.getPayoutId());
            record.setAmount(req.getAmount());
            record.setResult("REJECTED");
            record.setStatus("REJECTED");
            record.setCreatedAt(LocalDateTime.now());
            payoutStore.put(req.getPayoutId(), record);

            return new PayoutResponse("REJECTED", null, "BENEFICIARY_ACCOUNT_INVALID");
        }

        // ACCEPTED case
        String bankRef = "BR-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        registerAcceptedPayout(req, false);
        
        BankPayoutRecord record = new BankPayoutRecord();
        record.setPayoutId(req.getPayoutId());
        record.setAmount(req.getAmount());
        record.setResult("ACCEPTED");
        record.setStatus("IN_PROGRESS");
        record.setBankRef(bankRef);
        record.setCreatedAt(LocalDateTime.now());
        payoutStore.put(req.getPayoutId(), record);

        // 4. Trigger asynchronous callback flow
        scheduleCallbacks(req.getPayoutId(), bankRef, req.getAmount());

        return new PayoutResponse("ACCEPTED", bankRef, null);
    }

    public BankPayoutRecord getPayoutStatus(String payoutId) {
        sleep(1000 + random.nextInt(2000)); // 1 to 3 seconds view latency
        return payoutStore.get(payoutId);
    }

    public int getTotalPaid() {
        return totalPaidSantim.get();
    }

    private void registerAcceptedPayout(PayoutRequest req, boolean isTimeoutProcessed) {
        totalPaidSantim.addAndGet(req.getAmount());
    }

    private void scheduleCallbacks(String payoutId, String bankRef, int amount) {
        new Thread(() -> {
            try {
                // Callback arrives 1 to 10 seconds later
                long delay = (1 + random.nextInt(10)) * 1000L;
                Thread.sleep(delay);

                // Decide if accepted payout fails later (~5%)
                boolean callbackFails = random.nextDouble() < callbackFailureRate;
                String finalStatus = callbackFails ? "FAILED" : "SUCCEEDED";

                // Update internal status
                BankPayoutRecord record = payoutStore.get(payoutId);
                if (record != null) {
                    record.setStatus(finalStatus);
                }

                // Send callback to random M-Pesa instance (8081 or 8082)
                int targetPort = random.nextBoolean() ? 8081 : 8082;
                String callbackUrl = "http://localhost:" + targetPort + "/mpesa/v1/callbacks/bank";

                // Send 1 to 3 times concurrently (~30% duplicate rate)
                int deliveryCount = 1;
                if (random.nextDouble() < duplicateCallbackRate) {
                    deliveryCount = 2 + random.nextInt(2); // 2 or 3 times
                }

                String eventId = UUID.randomUUID().toString();
                for (int i = 0; i < deliveryCount; i++) {
                    final int attempt = i;
                    new Thread(() -> {
                        try {
                            Map<String, Object> payload = Map.of(
                                    "event_id", eventId,
                                    "payout_id", payoutId,
                                    "bank_ref", bankRef,
                                    "status", finalStatus
                            );
                            restTemplate.postForEntity(callbackUrl, payload, Void.class);
                        } catch (Exception ignored) {}
                    }).start();
                    if (attempt > 0) {
                        Thread.sleep(50); // slight stagger for duplicates
                    }
                }

            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}