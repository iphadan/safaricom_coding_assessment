package com.safaricom.mpessa_coding_interview.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WalletStatusUpdateResponse {
    private String walletId;
    private String status;
    private LocalDateTime changedAt;
}