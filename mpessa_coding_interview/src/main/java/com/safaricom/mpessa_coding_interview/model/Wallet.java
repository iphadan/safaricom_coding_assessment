package com.safaricom.mpessa_coding_interview.model;

import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private WalletStatus status = WalletStatus.ACTIVE;
    private int balance;
    private int reserved;
    private int available;
    @CreationTimestamp
    private LocalDateTime created_at;
}
