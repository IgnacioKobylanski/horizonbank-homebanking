package com.horizonbank.homebanking.transfers.application;

import com.horizonbank.homebanking.accounts.AccountsApi;
import com.horizonbank.homebanking.accounts.AccountsApi.AccountRef;
import com.horizonbank.homebanking.ledger.LedgerApi;
import com.horizonbank.homebanking.shared.error.BusinessRuleException;
import com.horizonbank.homebanking.transfers.infrastructure.persistence.IdempotencyKeyEntity;
import com.horizonbank.homebanking.transfers.infrastructure.persistence.IdempotencyKeyJpaRepository;
import com.horizonbank.homebanking.transfers.infrastructure.persistence.TransferEntity;
import com.horizonbank.homebanking.transfers.infrastructure.persistence.TransferJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountsApi accountsApi;
    private final LedgerApi ledgerApi;
    private final TransferJpaRepository transfers;
    private final IdempotencyKeyJpaRepository idempotencyKeys;

    @Transactional
    public TransferResult transfer(TransferCommand command) {
        BigDecimal amount = command.amount();
        if (amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessRuleException("Invalid amount");
        }
        String concept = normalize(command.concept());
        String fingerprint = fingerprint(command, concept);

        boolean claimed = idempotencyKeys.claim(UUID.randomUUID(), command.userId(),
                command.idempotencyKey(), fingerprint) == 1;
        IdempotencyKeyEntity record = idempotencyKeys
                .findByUserIdAndIdempotencyKey(command.userId(), command.idempotencyKey())
                .orElseThrow(() -> new IllegalStateException("Idempotency record missing"));

        if (!claimed) {
            return replay(record, fingerprint);
        }

        AccountRef source = accountsApi.requireActiveOwned(command.customerId(), command.sourceAccountId());
        AccountRef destination = accountsApi.findActiveByNumberOrAlias(command.destination());
        if (source.id().equals(destination.id())) {
            throw new BusinessRuleException("Source and destination must be different accounts");
        }
        if (!source.currencyCode().equals(destination.currencyCode())) {
            throw new BusinessRuleException("Accounts must share the same currency");
        }

        UUID ledgerTransactionId = ledgerApi.recordTransfer(
                source.id(), destination.id(), amount, concept == null ? "Transfer" : concept);
        TransferEntity transfer = transfers.save(new TransferEntity(
                source.id(), destination.id(), amount, concept, ledgerTransactionId));
        record.attachTransfer(transfer.getId());

        return new TransferResult(toView(transfer), false);
    }

    private TransferResult replay(IdempotencyKeyEntity record, String fingerprint) {
        if (!record.getRequestHash().equals(fingerprint)) {
            throw new BusinessRuleException("Idempotency-Key was already used with a different request");
        }
        if (record.getTransferId() == null) {
            throw new IllegalStateException("Idempotency record without transfer");
        }
        TransferEntity existing = transfers.findById(record.getTransferId())
                .orElseThrow(() -> new IllegalStateException("Transfer missing for idempotency record"));
        return new TransferResult(toView(existing), true);
    }

    private String normalize(String concept) {
        if (concept == null || concept.isBlank()) {
            return null;
        }
        return concept.trim();
    }

    private String fingerprint(TransferCommand command, String concept) {
        String canonical = String.join("|",
                command.sourceAccountId().toString(),
                command.destination().trim().toLowerCase(),
                command.amount().stripTrailingZeros().toPlainString(),
                concept == null ? "" : concept);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private TransferView toView(TransferEntity transfer) {
        return new TransferView(transfer.getId(), transfer.getSourceAccountId(),
                transfer.getDestinationAccountId(),
                transfer.getAmount().setScale(2, RoundingMode.UNNECESSARY),
                transfer.getStatus(), transfer.getConcept(), transfer.getCreatedAt());
    }
}