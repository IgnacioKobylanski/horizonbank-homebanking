package com.horizonbank.homebanking.ledger.application;

import com.horizonbank.homebanking.accounts.AccountsApi;
import com.horizonbank.homebanking.accounts.AccountsApi.AccountRef;
import com.horizonbank.homebanking.accounts.AccountsApi.Movement;
import com.horizonbank.homebanking.ledger.domain.EntryDirection;
import com.horizonbank.homebanking.ledger.domain.LedgerTransactionType;
import com.horizonbank.homebanking.ledger.infrastructure.persistence.LedgerEntryEntity;
import com.horizonbank.homebanking.ledger.infrastructure.persistence.LedgerEntryJpaRepository;
import com.horizonbank.homebanking.ledger.infrastructure.persistence.LedgerTransactionEntity;
import com.horizonbank.homebanking.ledger.infrastructure.persistence.LedgerTransactionJpaRepository;
import com.horizonbank.homebanking.shared.error.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private static final BigDecimal MAX_DEPOSIT = new BigDecimal("1000000");

    private final AccountsApi accountsApi;
    private final LedgerTransactionJpaRepository transactions;
    private final LedgerEntryJpaRepository entries;

    @Transactional
    public DepositReceipt deposit(UUID customerId, UUID accountId, BigDecimal amount) {
        if (amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessRuleException("Invalid amount");
        }
        if (amount.compareTo(MAX_DEPOSIT) > 0) {
            throw new BusinessRuleException("Amount exceeds the limit per deposit");
        }

        AccountRef target = accountsApi.requireActiveOwned(customerId, accountId);
        AccountRef cash = accountsApi.systemCashAccount(target.currencyCode());

        LedgerTransactionEntity transaction = transactions.save(
                new LedgerTransactionEntity(LedgerTransactionType.DEPOSIT, "Simulated deposit"));
        entries.saveAll(List.of(
                new LedgerEntryEntity(transaction.getId(), cash.id(), EntryDirection.DEBIT, amount),
                new LedgerEntryEntity(transaction.getId(), target.id(), EntryDirection.CREDIT, amount)));

        Map<UUID, BigDecimal> newBalances = accountsApi.applyMovements(List.of(
                new Movement(cash.id(), amount.negate()),
                new Movement(target.id(), amount)));

        return new DepositReceipt(transaction.getId(), target.id(), amount, newBalances.get(target.id()));
    }
}