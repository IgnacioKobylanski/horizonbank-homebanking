package com.horizonbank.homebanking.ledger.application;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositReceipt(UUID transactionId, UUID accountId, BigDecimal amount, BigDecimal balance) {
}