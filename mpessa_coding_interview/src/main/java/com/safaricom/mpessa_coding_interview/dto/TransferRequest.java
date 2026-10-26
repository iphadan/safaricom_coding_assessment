package com.safaricom.mpessa_coding_interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {

    @NotBlank(message = "from_wallet is required")
    private String fromWallet;

    @NotBlank(message = "to_wallet is required")
    private String toWallet;

    @Min(value = 1, message = "Amount must be greater than 0")
    @Max(value = 5_000_000, message = "Amount exceeds single transfer limit")
    private int amount;

    @NotBlank(message = "Reference is required")
    private String reference;
}