package com.safaricom.mpessa_coding_interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TransferRequest {

    @NotBlank
    @JsonProperty("from_wallet")
    private String fromWallet;

    @NotBlank
    @JsonProperty("to_wallet")
    private String toWallet;

    @Min(1)
    private int amount;

    private String reference;
}