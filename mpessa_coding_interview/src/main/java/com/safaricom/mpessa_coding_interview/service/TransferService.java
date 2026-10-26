package com.safaricom.mpessa_coding_interview.service;

import com.safaricom.mpessa_coding_interview.dto.TransferRequest;
import com.safaricom.mpessa_coding_interview.dto.TransferResponse;
import com.safaricom.mpessa_coding_interview.enums.TransferStatus;
import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import com.safaricom.mpessa_coding_interview.model.LedgerEntry;
import com.safaricom.mpessa_coding_interview.model.Transfer;
import com.safaricom.mpessa_coding_interview.model.Wallet;
import com.safaricom.mpessa_coding_interview.repo.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepo walletRepo;
    private final TransferRepo transferRepo;
    private final LedgerEntryRepo ledgerEntryRepo;
    private final IdempotencyService idempotencyService;

    public TransferService(WalletRepo walletRepo, TransferRepo transferRepo, 
                           LedgerEntryRepo ledgerEntryRepo, IdempotencyRepo idempotencyRepo
    , IdempotencyService idempotencyService) {
        this.walletRepo = walletRepo;
        this.transferRepo = transferRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    public TransferResponse executeTransfer(String idempotencyKey, TransferRequest req) {
        return idempotencyService.executeWithIdempotency(idempotencyKey, req, TransferResponse.class, () -> {   // Validation checks
        if (req.getAmount() <= 0 || req.getAmount() > 5_000_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid amount");
        }
        if (req.getReference() == null || req.getReference().length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reference");
        }
        if (req.getFromWallet().equals(req.getToWallet())) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "SAME_WALLET");
        }

        // Idempotency check handling
        // (In a complete implementation, hash the request body and check IdempotencyRecord)

        // Deadlock Prevention: Lock wallets in consistent sorted order
        String firstId = req.getFromWallet().compareTo(req.getToWallet()) < 0 ? req.getFromWallet() : req.getToWallet();
        String secondId = firstId.equals(req.getFromWallet()) ? req.getToWallet() : req.getFromWallet();

        Wallet firstWallet = walletRepo.findByIdWithLock(firstId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found: " + firstId));
        Wallet secondWallet = walletRepo.findByIdWithLock(secondId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found: " + secondId));

        Wallet sender = req.getFromWallet().equals(firstWallet.getId()) ? firstWallet : secondWallet;
        Wallet receiver = req.getToWallet().equals(firstWallet.getId()) ? firstWallet : secondWallet;

        // Check active status
        if (sender.getStatus() != WalletStatus.ACTIVE || receiver.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "WALLET_NOT_ACTIVE");
        }

        // Check sufficient funds (available = balance - reserved)
        if (sender.getAvailable() < req.getAmount()) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "INSUFFICIENT_FUNDS");
        }

        // Update balances atomically
        sender.setBalance(sender.getBalance() - req.getAmount());
        sender.setAvailable(sender.getBalance() - sender.getReserved());

        receiver.setBalance(receiver.getBalance() + req.getAmount());
        receiver.setAvailable(receiver.getBalance() - receiver.getReserved());

        walletRepo.save(sender);
        walletRepo.save(receiver);

        // Record Transfer
        String transferId = "T-" + UUID.randomUUID().toString().substring(0, 8);
        Transfer transfer = new Transfer();
        transfer.setId(transferId);
        transfer.setFromWallet(sender.getId());
        transfer.setToWallet(receiver.getId());
        transfer.setAmount(req.getAmount());
        transfer.setReference(req.getReference());
        transfer.setStatus(TransferStatus.COMPLETED);
        transferRepo.save(transfer);

        // Audit Trail Ledger Entries
        createLedgerEntry(sender.getId(), "TRANSFER_OUT", -req.getAmount(), sender.getBalance(), transferId);
        createLedgerEntry(receiver.getId(), "TRANSFER_IN", req.getAmount(), receiver.getBalance(), transferId);

        TransferResponse response = new TransferResponse();
        response.setTransferId(transferId);
        response.setStatus("COMPLETED");
        response.setFromAvailableAfter(sender.getAvailable());
        response.setCreatedAt(LocalDateTime.now());

        return response;
        });
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