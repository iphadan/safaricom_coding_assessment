package com.safaricom.mpessa_coding_interview.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.Transaction;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
public class Reversal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String reversal_id;
    @OneToOne
    @JoinColumn(name = "transfer_id")
    private Transfer transfer;
    @CreationTimestamp
private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


