package com.horizonbank.homebanking.transfers.infrastructure.persistence;

import com.horizonbank.homebanking.transfers.domain.TransferStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_account_id", nullable = false, updatable = false)
    private UUID sourceAccountId;

    @Column(name = "destination_account_id", nullable = false, updatable = false)
    private UUID destinationAccountId;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(length = 140)
    private String concept;

    @Column(name = "ledger_transaction_id")
    private UUID ledgerTransactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public TransferEntity(UUID sourceAccountId, UUID destinationAccountId, BigDecimal amount,
                          String concept, UUID ledgerTransactionId) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.concept = concept;
        this.ledgerTransactionId = ledgerTransactionId;
        this.status = TransferStatus.COMPLETED;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}