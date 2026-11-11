package com.horizonbank.homebanking.transfers.application;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferCommand(
        UUID userId,
        UUID customerId,
        String idempotencyKey,
        UUID sourceAccountId,
        String destination,
        BigDecimal amount,
        String concept
) {
}