package com.horizonbank.homebanking.accounts.infrastructure.persistence;

import com.horizonbank.homebanking.accounts.domain.AccountStatus;
import com.horizonbank.homebanking.accounts.domain.AccountType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "account_number", nullable = false, length = 22)
    private String accountNumber;

    @Column(length = 30)
    private String alias;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public AccountEntity(UUID customerId, String accountNumber, String alias,
                         String currencyCode, AccountType type) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.alias = alias;
        this.currencyCode = currencyCode;
        this.type = type;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}