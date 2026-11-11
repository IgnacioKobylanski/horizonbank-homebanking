package com.horizonbank.homebanking.transfers.application;

import com.horizonbank.homebanking.transfers.domain.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferView(
        UUID id,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        TransferStatus status,
        String concept,
        Instant createdAt
) {
}