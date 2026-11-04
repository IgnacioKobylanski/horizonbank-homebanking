package com.horizonbank.homebanking.accounts.application;

import com.horizonbank.homebanking.accounts.AccountsApi;
import com.horizonbank.homebanking.accounts.domain.AccountStatus;
import com.horizonbank.homebanking.accounts.domain.AccountType;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountBalanceEntity;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountBalanceJpaRepository;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountEntity;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountJpaRepository;
import com.horizonbank.homebanking.shared.error.BusinessRuleException;
import com.horizonbank.homebanking.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountsApiImpl implements AccountsApi {

    private final AccountJpaRepository accounts;
    private final AccountBalanceJpaRepository balances;

    @Override
    public AccountRef requireActiveOwned(UUID customerId, UUID accountId) {
        AccountEntity account = accounts.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Account is not active");
        }
        return new AccountRef(account.getId(), account.getCurrencyCode());
    }

    @Override
    public AccountRef systemCashAccount(String currencyCode) {
        AccountEntity cash = accounts.findByAliasIgnoreCase("SYSTEM.CASH." + currencyCode)
                .orElseThrow(() -> new IllegalStateException(
                        "System cash account missing for " + currencyCode));
        return new AccountRef(cash.getId(), cash.getCurrencyCode());
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Map<UUID, BigDecimal> applyMovements(List<Movement> movements) {
        Map<UUID, BigDecimal> deltas = new HashMap<>();
        for (Movement movement : movements) {
            deltas.merge(movement.accountId(), movement.delta(), BigDecimal::add);
        }

        List<AccountBalanceEntity> locked = balances.findAllForUpdate(deltas.keySet());
        if (locked.size() != deltas.size()) {
            throw new IllegalStateException("Missing balance row");
        }
        Map<UUID, AccountType> types = accounts.findAllById(deltas.keySet()).stream()
                .collect(Collectors.toMap(AccountEntity::getId, AccountEntity::getType));

        Map<UUID, BigDecimal> result = new LinkedHashMap<>();
        for (AccountBalanceEntity balance : locked) {
            balance.apply(deltas.get(balance.getAccountId()));
            boolean isSystem = types.get(balance.getAccountId()) == AccountType.SYSTEM;
            if (!isSystem && balance.getBalance().signum() < 0) {
                throw new BusinessRuleException("Insufficient funds");
            }
            result.put(balance.getAccountId(), balance.getBalance());
        }
        return result;
    }
}