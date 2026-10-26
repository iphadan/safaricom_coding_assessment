package com.safaricom.mpessa_coding_interview.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String transfer_id;
    private String from_wallet;
    private String to_wallet;
    private int amount;
    private String reference;
    @CreationTimestamp
    private LocalDateTime created_at;


}
