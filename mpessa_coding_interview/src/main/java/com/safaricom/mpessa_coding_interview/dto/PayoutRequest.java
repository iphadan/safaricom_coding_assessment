package com.safaricom.mpessa_coding_interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayoutRequest {

    @NotBlank(message = "wallet_id is required")
    private String walletId;

    @NotBlank(message = "bank_account is required")
    private String bankAccount;

    @Min(value = 1, message = "Amount must be greater than 0")
    @Max(value = 5_000_000, message = "Amount exceeds payout limit")
    private int amount;
}