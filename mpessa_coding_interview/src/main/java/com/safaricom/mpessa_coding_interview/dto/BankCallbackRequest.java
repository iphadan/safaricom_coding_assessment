package com.safaricom.mpessa_coding_interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BankCallbackRequest {

    @NotBlank(message = "payout_id is required")
    private String payoutId;

    @NotBlank(message = "status is required")
    private String status;

    private String bankRef;
}