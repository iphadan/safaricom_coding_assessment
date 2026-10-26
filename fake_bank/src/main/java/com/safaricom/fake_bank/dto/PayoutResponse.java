package com.safaricom.fake_bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PayoutResponse {
    private String result; // ACCEPTED, REJECTED
    @JsonProperty("bank_ref")
    private String bankRef;
    private String reason;
}