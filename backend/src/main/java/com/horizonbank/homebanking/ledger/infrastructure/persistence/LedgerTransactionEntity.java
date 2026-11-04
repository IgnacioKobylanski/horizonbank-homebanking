package com.horizonbank.homebanking.ledger.infrastructure.persistence;

import com.horizonbank.homebanking.ledger.domain.LedgerTransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "ledger_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerTransactionType type;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerTransactionEntity(LedgerTransactionType type, String description) {
        this.type = type;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}