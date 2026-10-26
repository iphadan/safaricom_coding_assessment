package com.safaricom.mpessa_coding_interview.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class Reversal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String reversal_id;
    private String transfer_id;
    @CreationTimestamp
private LocalDateTime created_t;
}


