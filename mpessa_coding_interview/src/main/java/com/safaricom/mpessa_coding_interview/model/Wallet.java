package com.safaricom.mpessa_coding_interview.model;

import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_status", columnList = "status")
})
@Data
public class Wallet {

    @Id
    @Column(length = 64)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(nullable = false)
    private int balance;

    @Column(nullable = false)
    private int reserved;

    @Column(nullable = false)
    private int available;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}