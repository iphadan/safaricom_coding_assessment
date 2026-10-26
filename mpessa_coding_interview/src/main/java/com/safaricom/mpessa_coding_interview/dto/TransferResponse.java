package com.safaricom.mpessa_coding_interview.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    private String transferId;
    private String status;
    private int fromAvailableAfter;
    private LocalDateTime createdAt;
}