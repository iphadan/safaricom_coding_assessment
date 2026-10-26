package com.safaricom.mpessa_coding_interview.controller;

import com.safaricom.mpessa_coding_interview.dto.BankCallbackRequest;
import com.safaricom.mpessa_coding_interview.dto.PayoutCreateResponse;
import com.safaricom.mpessa_coding_interview.dto.PayoutRequest;
import com.safaricom.mpessa_coding_interview.dto.PayoutResponse;
import com.safaricom.mpessa_coding_interview.service.PayoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mpesa/v1/payouts")
public class PayoutController {

    private final PayoutService payoutService;

    public PayoutController(PayoutService payoutService) {
        this.payoutService = payoutService;
    }

    @PostMapping("/payouts")
    public ResponseEntity<PayoutCreateResponse> createPayout(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PayoutRequest request) {
        PayoutCreateResponse response = payoutService.createPayout(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @PostMapping("/callbacks/bank")
    public ResponseEntity<Void> handleBankCallback(@Valid @RequestBody BankCallbackRequest request) {
        payoutService.handleBankCallback(request);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<PayoutResponse> getPayoutStatus(@PathVariable("id") String payoutId) {
        PayoutResponse response = payoutService.getPayoutStatus(payoutId);
        return ResponseEntity.ok(response);
    }
}