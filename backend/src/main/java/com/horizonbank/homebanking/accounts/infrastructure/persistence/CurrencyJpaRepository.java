package com.horizonbank.homebanking.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyJpaRepository extends JpaRepository<CurrencyEntity, String> {
}