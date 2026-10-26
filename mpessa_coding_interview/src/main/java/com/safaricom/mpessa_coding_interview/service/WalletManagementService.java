package com.safaricom.mpessa_coding_interview.service;

import com.safaricom.mpessa_coding_interview.dto.WalletSnapshotResponse;
import com.safaricom.mpessa_coding_interview.dto.WalletStatusRequest;
import com.safaricom.mpessa_coding_interview.dto.WalletStatusUpdateResponse;
import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import com.safaricom.mpessa_coding_interview.model.Wallet;
import com.safaricom.mpessa_coding_interview.repo.WalletRepo;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class WalletManagementService {

    private final WalletRepo walletRepo;

    public WalletManagementService(WalletRepo walletRepo) {
        this.walletRepo = walletRepo;
    }

    public WalletSnapshotResponse getWallet(String id) {
        Wallet wallet = walletRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        WalletSnapshotResponse res = new WalletSnapshotResponse();
        res.setWalletId(wallet.getId());
        res.setStatus(wallet.getStatus().name());
        res.setBalance(wallet.getBalance());
        res.setReserved(wallet.getReserved());
        res.setAvailable(wallet.getBalance() - wallet.getReserved());
        return res;
    }

    @Transactional
    public WalletStatusUpdateResponse updateWalletStatus(String id, WalletStatusRequest req) {
        Wallet wallet = walletRepo.findByIdWithLock(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        WalletStatus targetStatus;
        try {
            targetStatus = WalletStatus.valueOf(req.getStatus());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }

        wallet.setStatus(targetStatus);
        walletRepo.save(wallet);

        WalletStatusUpdateResponse res = new WalletStatusUpdateResponse();
        res.setWalletId(wallet.getId());
        res.setStatus(wallet.getStatus().name());
        res.setChangedAt(LocalDateTime.now());
        return res;
    }
}