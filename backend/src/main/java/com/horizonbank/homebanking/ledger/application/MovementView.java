package com.horizonbank.homebanking.ledger.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MovementView(
        long id,
        UUID transactionId,
        String type,
        String description,
        String direction,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {
}