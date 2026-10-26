package com.safaricom.mpessa_coding_interview.model;

import com.safaricom.mpessa_coding_interview.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_transfer_from", columnList = "from_wallet"),
        @Index(name = "idx_transfer_to", columnList = "to_wallet")
})
@Data
public class Transfer {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "from_wallet", nullable = false, length = 64)
    private String fromWallet;

    @Column(name = "to_wallet", nullable = false, length = 64)
    private String toWallet;

    @Column(nullable = false)
    private int amount; // Most amount balance and reserved should be placed in BgDecimal

    @Column(nullable = false, length = 64)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status = TransferStatus.COMPLETED;

    @Column(name = "reversal_id", length = 64)
    private String reversalId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}