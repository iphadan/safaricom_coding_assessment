package com.safaricom.mpessa_coding_interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WalletStatusRequest {
    @NotBlank(message = "Status is required (ACTIVE or FROZEN)")
    private String status;
    private String reason;
}