package com.horizonbank.homebanking.accounts.application;

import com.horizonbank.homebanking.accounts.domain.AccountType;

import java.util.UUID;

public record CreateAccountCommand(
        UUID customerId,
        String currencyCode,
        AccountType type,
        String alias
) {
}