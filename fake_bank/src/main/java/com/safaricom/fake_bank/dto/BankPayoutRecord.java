package com.safaricom.fake_bank.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BankPayoutRecord {
    private String payoutId;
    private int amount;
    private String result; // ACCEPTED, REJECTED
    private String status; // IN_PROGRESS, SUCCEEDED, FAILED, REJECTED
    private String bankRef;
    private LocalDateTime createdAt;
}