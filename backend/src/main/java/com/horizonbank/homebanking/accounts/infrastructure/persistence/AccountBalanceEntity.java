package com.horizonbank.homebanking.accounts.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_balances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBalanceEntity {

    @Id
    @Column(name = "account_id")
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public AccountBalanceEntity(UUID accountId) {
        this.accountId = accountId;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}