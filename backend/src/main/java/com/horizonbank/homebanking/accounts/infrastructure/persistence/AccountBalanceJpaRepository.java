package com.horizonbank.homebanking.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountBalanceJpaRepository extends JpaRepository<AccountBalanceEntity, UUID> {
}