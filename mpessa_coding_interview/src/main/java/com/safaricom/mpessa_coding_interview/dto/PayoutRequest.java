package com.safaricom.mpessa_coding_interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayoutRequest {

    @NotBlank
    @JsonProperty("payout_id")
    private String payoutId;

    @NotBlank
    @JsonProperty("wallet_id")
    private String walletId;

    @NotBlank
    @JsonProperty("bank_account")
    private String bankAccount;

    @Min(1)
    private int amount;
}