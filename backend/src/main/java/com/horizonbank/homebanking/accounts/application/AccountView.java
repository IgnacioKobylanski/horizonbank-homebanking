package com.horizonbank.homebanking.accounts.application;

import com.horizonbank.homebanking.accounts.domain.AccountStatus;
import com.horizonbank.homebanking.accounts.domain.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountView(
        UUID id,
        String accountNumber,
        String alias,
        String currencyCode,
        AccountType type,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt
) {
}