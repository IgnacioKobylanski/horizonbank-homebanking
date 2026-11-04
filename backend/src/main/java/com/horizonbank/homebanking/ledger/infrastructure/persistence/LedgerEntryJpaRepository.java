package com.horizonbank.homebanking.ledger.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, Long> {
}