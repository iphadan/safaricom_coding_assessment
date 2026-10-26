package com.safaricom.mpessa_coding_interview.controller;

import com.safaricom.mpessa_coding_interview.dto.WalletSnapshotResponse;
import com.safaricom.mpessa_coding_interview.dto.WalletStatusRequest;
import com.safaricom.mpessa_coding_interview.dto.WalletStatusUpdateResponse;
import com.safaricom.mpessa_coding_interview.service.WalletManagementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mpesa/v1/wallets")
public class WalletController {

    private final WalletManagementService walletService;

    public WalletController(WalletManagementService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletSnapshotResponse> getWallet(@PathVariable String id) {
        return ResponseEntity.ok(walletService.getWallet(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WalletStatusUpdateResponse> updateWalletStatus(
            @PathVariable String id,
            @Valid @RequestBody WalletStatusRequest request) {
        return ResponseEntity.ok(walletService.updateWalletStatus(id, request));
    }
}