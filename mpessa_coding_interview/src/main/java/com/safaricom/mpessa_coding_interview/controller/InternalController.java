package com.safaricom.mpessa_coding_interview.controller;

import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import com.safaricom.mpessa_coding_interview.model.Wallet;
import com.safaricom.mpessa_coding_interview.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/mpesa/internal")
public class InternalController {

    private final WalletRepo walletRepo;
    private final TransferRepo transferRepo;
    private final PayoutRepo payoutRepo;
    private final IdempotencyRepo idempotencyRepo;
    private final LedgerEntryRepo ledgerEntryRepo;

    public InternalController(WalletRepo walletRepo, TransferRepo transferRepo, 
                              PayoutRepo payoutRepo, IdempotencyRepo idempotencyRepo, 
                              LedgerEntryRepo ledgerEntryRepo) {
        this.walletRepo = walletRepo;
        this.transferRepo = transferRepo;
        this.payoutRepo = payoutRepo;
        this.idempotencyRepo = idempotencyRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<Void> resetData() {
        // Clear tables in order of foreign key dependencies
        ledgerEntryRepo.deleteAll();
        transferRepo.deleteAll();
        payoutRepo.deleteAll();
        idempotencyRepo.deleteAll();
        walletRepo.deleteAll();

        List<Wallet> wallets = new ArrayList<>();

        // W001 to W100 (ACTIVE, 1,000,000 santim)
        for (int i = 1; i <= 100; i++) {
            String id = String.format("W%03d", i);
            wallets.add(createWallet(id, WalletStatus.ACTIVE, 1_000_000));
        }

        // W101 to W105 (FROZEN, 1,000,000 santim)
        for (int i = 101; i <= 105; i++) {
            String id = String.format("W%03d", i);
            wallets.add(createWallet(id, WalletStatus.FROZEN, 1_000_000));
        }

        // M001 (ACTIVE merchant, 50,000,000 santim)
        wallets.add(createWallet("M001", WalletStatus.ACTIVE, 50_000_000));

        walletRepo.saveAll(wallets);
        return ResponseEntity.ok().build();
    }

    private Wallet createWallet(String id, WalletStatus status, int balance) {
        Wallet w = new Wallet();
        w.setId(id);
        w.setStatus(status);
        w.setBalance(balance);
        w.setReserved(0);
        w.setAvailable(balance);
        return w;
    }
}