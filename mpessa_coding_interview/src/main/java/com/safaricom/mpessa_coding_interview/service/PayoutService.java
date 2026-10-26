package com.safaricom.mpessa_coding_interview.service;

import com.safaricom.mpessa_coding_interview.dto.BankCallbackRequest;
import com.safaricom.mpessa_coding_interview.dto.PayoutCreateResponse;
import com.safaricom.mpessa_coding_interview.dto.PayoutRequest;
import com.safaricom.mpessa_coding_interview.enums.PayoutStatus;
import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import com.safaricom.mpessa_coding_interview.model.LedgerEntry;
import com.safaricom.mpessa_coding_interview.model.Payout;
import com.safaricom.mpessa_coding_interview.model.Wallet;
import com.safaricom.mpessa_coding_interview.repo.LedgerEntryRepo;
import com.safaricom.mpessa_coding_interview.repo.PayoutRepo;
import com.safaricom.mpessa_coding_interview.repo.WalletRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PayoutService {

    private final WalletRepo walletRepo;
    private final PayoutRepo payoutRepo;
    private final LedgerEntryRepo ledgerEntryRepo;

    public PayoutService(WalletRepo walletRepo, PayoutRepo payoutRepo, LedgerEntryRepo ledgerEntryRepo) {
        this.walletRepo = walletRepo;
        this.payoutRepo = payoutRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
    }

    @Transactional
    public PayoutCreateResponse createPayout(PayoutRequest req) {
        if (req.getAmount() <= 0 || req.getAmount() > 5_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }

        Wallet wallet = walletRepo.findByIdWithLock(req.getWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "WALLET_NOT_ACTIVE");
        }
        if (wallet.getAvailable() < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "INSUFFICIENT_FUNDS");
        }

        // Reserve funds
        wallet.setReserved(wallet.getReserved() + req.getAmount());
        wallet.setAvailable(wallet.getBalance() - wallet.getReserved());
        walletRepo.save(wallet);

        String payoutId = "P-" + UUID.randomUUID().toString().substring(0, 8);
        Payout payout = new Payout();
        payout.setPayoutId(payoutId);
        payout.setWalletId(wallet.getId());
        payout.setBankAccount(req.getBankAccount());
        payout.setAmount(req.getAmount());
        payout.setStatus(PayoutStatus.PENDING);
        payoutRepo.save(payout);

        PayoutCreateResponse res = new PayoutCreateResponse();
        res.setPayoutId(payoutId);
        res.setStatus(PayoutStatus.PENDING.name());
        return res;
    }

    @Transactional
    public void handleBankCallback(BankCallbackRequest req) {
        Payout payout = payoutRepo.findById(req.getPayoutId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout not found"));

        Wallet wallet = walletRepo.findByIdWithLock(payout.getWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        PayoutStatus incomingStatus;
        try {
            incomingStatus = PayoutStatus.valueOf(req.getStatus());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid callback status");
        }

        if (payout.getStatus() == PayoutStatus.SUCCEEDED || payout.getStatus() == PayoutStatus.FAILED) {
            if (payout.getStatus() != incomingStatus) {
                payout.setConflict(true);
                payoutRepo.save(payout);
            }
            return;
        }

        if (incomingStatus == PayoutStatus.SUCCEEDED) {
            wallet.setBalance(wallet.getBalance() - payout.getAmount());
            wallet.setReserved(wallet.getReserved() - payout.getAmount());
            wallet.setAvailable(wallet.getBalance() - wallet.getReserved());
            walletRepo.save(wallet);

            payout.setStatus(PayoutStatus.SUCCEEDED);
            payout.setBankRef(req.getBankRef());
            payoutRepo.save(payout);

            createLedgerEntry(wallet.getId(), "PAYOUT", -payout.getAmount(), wallet.getBalance(), payout.getPayoutId());

        } else if (incomingStatus == PayoutStatus.FAILED) {
            wallet.setReserved(wallet.getReserved() - payout.getAmount());
            wallet.setAvailable(wallet.getBalance() - wallet.getReserved());
            walletRepo.save(wallet);

            payout.setStatus(PayoutStatus.FAILED);
            payoutRepo.save(payout);
        }
    }

    private void createLedgerEntry(String walletId, String type, int amount, int balanceAfter, String ref) {
        LedgerEntry entry = new LedgerEntry();
        entry.setEntryId("E-" + UUID.randomUUID().toString().substring(0, 8));
        entry.setWalletId(walletId);
        entry.setType(type);
        entry.setAmount(amount);
        entry.setBalanceAfter(balanceAfter);
        entry.setReference(ref);
        ledgerEntryRepo.save(entry);
    }
}