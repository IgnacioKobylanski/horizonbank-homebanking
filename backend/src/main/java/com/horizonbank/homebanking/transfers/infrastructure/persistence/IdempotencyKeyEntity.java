package com.horizonbank.homebanking.transfers.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKeyEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "idempotency_key", nullable = false, length = 100, updatable = false)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64, updatable = false)
    private String requestHash;

    @Column(name = "transfer_id")
    private UUID transferId;

    public void attachTransfer(UUID transferId) {
        this.transferId = transferId;
    }
}