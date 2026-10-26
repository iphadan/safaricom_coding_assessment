package com.safaricom.mpessa_coding_interview.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PayoutResponse {
    private String payoutId;
    private String walletId;
    private int amount;
    private String status;
    private String bankRef;
    private boolean conflict;
    private LocalDateTime updatedAt;
    private String reason;
}