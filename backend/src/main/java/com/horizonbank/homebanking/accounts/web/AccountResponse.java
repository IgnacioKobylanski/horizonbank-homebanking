package com.horizonbank.homebanking.accounts.web;

import com.horizonbank.homebanking.accounts.application.AccountView;
import com.horizonbank.homebanking.accounts.domain.AccountStatus;
import com.horizonbank.homebanking.accounts.domain.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String alias,
        String currency,
        AccountType type,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt
) {

    public static AccountResponse from(AccountView view) {
        return new AccountResponse(view.id(), view.accountNumber(), view.alias(),
                view.currencyCode(), view.type(), view.status(), view.balance(), view.createdAt());
    }
}