package com.safaricom.mpessa_coding_interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WalletSnapshotResponse {

    private String walletId;
    private String status;
    private int balance;
    private int reserved;
    private int available;
}