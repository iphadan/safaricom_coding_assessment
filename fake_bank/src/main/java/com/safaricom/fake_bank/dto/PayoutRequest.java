package com.safaricom.fake_bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PayoutRequest {
    @JsonProperty("payout_id")
    private String payoutId;

    @JsonProperty("bank_account")
    private String bankAccount;

    private int amount;
}