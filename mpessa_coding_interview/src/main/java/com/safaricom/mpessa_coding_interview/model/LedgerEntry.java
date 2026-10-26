package com.safaricom.mpessa_coding_interview.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_wallet_created", columnList = "wallet_id, created_at DESC")
})
@Data
public class LedgerEntry {

    @Id
    @Column(name = "entry_id", length = 64)
    private String entryId;

    @Column(name = "wallet_id", nullable = false, length = 64)
    private String walletId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false)
    private int amount;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    @Column(length = 64)
    private String reference;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}