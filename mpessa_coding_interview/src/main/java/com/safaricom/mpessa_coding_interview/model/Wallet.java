package com.safaricom.mpessa_coding_interview.model;

import com.safaricom.mpessa_coding_interview.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

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
    private int balance; // in santim

    @Column(nullable = false)
    private int reserved; // in santim (for in-flight payouts)

    @Column(nullable = false)
    private int available; // balance - reserved

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}