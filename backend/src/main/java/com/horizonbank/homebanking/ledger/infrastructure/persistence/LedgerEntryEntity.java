package com.horizonbank.homebanking.ledger.infrastructure.persistence;

import com.horizonbank.homebanking.ledger.domain.EntryDirection;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "ledger_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6, updatable = false)
    private EntryDirection direction;

    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerEntryEntity(UUID transactionId, UUID accountId,
                             EntryDirection direction, BigDecimal amount) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.direction = direction;
        this.amount = amount;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}