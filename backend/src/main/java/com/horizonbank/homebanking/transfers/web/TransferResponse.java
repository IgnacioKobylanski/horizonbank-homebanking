package com.horizonbank.homebanking.transfers.web;

import com.horizonbank.homebanking.transfers.application.TransferView;
import com.horizonbank.homebanking.transfers.domain.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        TransferStatus status,
        String concept,
        Instant createdAt
) {

    public static TransferResponse from(TransferView view) {
        return new TransferResponse(view.id(), view.sourceAccountId(), view.destinationAccountId(),
                view.amount(), view.status(), view.concept(), view.createdAt());
    }
}