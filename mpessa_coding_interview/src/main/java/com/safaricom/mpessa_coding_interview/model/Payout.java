package com.safaricom.mpessa_coding_interview.model;

import com.safaricom.mpessa_coding_interview.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payouts", indexes = {
    @Index(name = "idx_payout_wallet", columnList = "wallet_id"),
    @Index(name = "idx_payout_status", columnList = "status")
})
@Data
public class Payout {

    @Id
    @Column(name = "payout_id", length = 64)
    private String payoutId;

    @Column(name = "wallet_id", nullable = false, length = 64)
    private String walletId;

    @Column(name = "bank_account", nullable = false, length = 64)
    private String bankAccount;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "bank_ref", length = 128)
    private String bankRef;
private String reason;
    @Column(nullable = false)
    private boolean conflict = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}