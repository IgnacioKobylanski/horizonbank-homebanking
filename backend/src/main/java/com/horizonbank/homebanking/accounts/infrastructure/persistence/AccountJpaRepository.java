package com.horizonbank.homebanking.accounts.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    List<AccountEntity> findAllByCustomerIdOrderByCreatedAtAsc(UUID customerId);

    Optional<AccountEntity> findByIdAndCustomerId(UUID id, UUID customerId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAliasIgnoreCase(String alias);
}