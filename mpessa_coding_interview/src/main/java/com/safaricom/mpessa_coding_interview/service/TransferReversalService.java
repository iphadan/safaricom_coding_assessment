package com.safaricom.mpessa_coding_interview.service;

import com.safaricom.mpessa_coding_interview.dto.ReversalResponse;
import com.safaricom.mpessa_coding_interview.enums.TransferStatus;
import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import com.safaricom.mpessa_coding_interview.model.LedgerEntry;
import com.safaricom.mpessa_coding_interview.model.Transfer;
import com.safaricom.mpessa_coding_interview.model.Wallet;
import com.safaricom.mpessa_coding_interview.repo.LedgerEntryRepo;
import com.safaricom.mpessa_coding_interview.repo.TransferRepo;
import com.safaricom.mpessa_coding_interview.repo.WalletRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TransferReversalService {

    private final TransferRepo transferRepo;
    private final WalletRepo walletRepo;
    private final LedgerEntryRepo ledgerEntryRepo;

    public TransferReversalService(TransferRepo transferRepo, WalletRepo walletRepo, LedgerEntryRepo ledgerEntryRepo) {
        this.transferRepo = transferRepo;
        this.walletRepo = walletRepo;
        this.ledgerEntryRepo = ledgerEntryRepo;
    }

    @Transactional
    public ReversalResponse reverseTransfer(String transferId) {
        Transfer transfer = transferRepo.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));

        if (transfer.getStatus() != TransferStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "NOT_REVERSIBLE");
        }
        if (transfer.getReversalId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ALREADY_REVERSED");
        }

        // Lock wallets in lexicographical order to prevent deadlocks
        String firstId = transfer.getToWallet().compareTo(transfer.getFromWallet()) < 0 ? transfer.getToWallet() : transfer.getFromWallet();
        String secondId = firstId.equals(transfer.getToWallet()) ? transfer.getFromWallet() : transfer.getToWallet();

        Wallet firstWallet = walletRepo.findByIdWithLock(firstId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        Wallet secondWallet = walletRepo.findByIdWithLock(secondId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        Wallet debiter = transfer.getToWallet().equals(firstWallet.getId()) ? firstWallet : secondWallet;
        Wallet crediter = transfer.getFromWallet().equals(firstWallet.getId()) ? firstWallet : secondWallet;

        // Debited wallet must be ACTIVE and have enough available funds
        if (debiter.getStatus() != WalletStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "WALLET_NOT_ACTIVE");
        }
        if (debiter.getAvailable() < transfer.getAmount()) {
            throw new ResponseStatusException(HttpStatus.valueOf(422), "INSUFFICIENT_FUNDS");
        }
        // Credited wallet may be FROZEN (allowed)

        // Execute Reversal Balance Adjustments
        debiter.setBalance(debiter.getBalance() - transfer.getAmount());
        debiter.setAvailable(debiter.getBalance() - debiter.getReserved());

        crediter.setBalance(crediter.getBalance() + transfer.getAmount());
        crediter.setAvailable(crediter.getBalance() - crediter.getReserved());

        walletRepo.save(debiter);
        walletRepo.save(crediter);

        String reversalId = "R-" + UUID.randomUUID().toString().substring(0, 8);
        transfer.setStatus(TransferStatus.REVERSED);
        transfer.setReversalId(reversalId);
        transferRepo.save(transfer);

        // Audit Ledger Entries
        createLedgerEntry(debiter.getId(), "REVERSAL_OUT", -transfer.getAmount(), debiter.getBalance(), reversalId);
        createLedgerEntry(crediter.getId(), "REVERSAL_IN", transfer.getAmount(), crediter.getBalance(), reversalId);

        ReversalResponse res = new ReversalResponse();
        res.setTransferId(transfer.getId());
        res.setReversalId(reversalId);
        res.setStatus("REVERSED");
        res.setAmount(transfer.getAmount());
        return res;
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