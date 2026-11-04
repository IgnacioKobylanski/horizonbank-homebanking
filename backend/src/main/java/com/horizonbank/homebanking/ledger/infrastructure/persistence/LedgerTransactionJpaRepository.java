package com.horizonbank.homebanking.ledger.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerTransactionJpaRepository extends JpaRepository<LedgerTransactionEntity, UUID> {
}