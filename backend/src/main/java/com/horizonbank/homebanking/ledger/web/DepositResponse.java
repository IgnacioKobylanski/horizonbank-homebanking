package com.horizonbank.homebanking.ledger.web;

import com.horizonbank.homebanking.ledger.application.DepositReceipt;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositResponse(UUID transactionId, UUID accountId, BigDecimal amount, BigDecimal balance) {

    public static DepositResponse from(DepositReceipt receipt) {
        return new DepositResponse(receipt.transactionId(), receipt.accountId(),
                receipt.amount(), receipt.balance());
    }
}