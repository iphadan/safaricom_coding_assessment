package com.safaricom.mpessa_coding_interview.dto;

import lombok.Data;

@Data
public class ReversalResponse {
    private String transferId;
    private String reversalId;
    private String status;
    private int amount;
}