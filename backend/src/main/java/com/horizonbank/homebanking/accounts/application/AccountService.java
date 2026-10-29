package com.horizonbank.homebanking.accounts.application;

import com.horizonbank.homebanking.accounts.domain.AccountType;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountBalanceEntity;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountBalanceJpaRepository;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountEntity;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountJpaRepository;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.CurrencyJpaRepository;
import com.horizonbank.homebanking.shared.error.BusinessRuleException;
import com.horizonbank.homebanking.shared.error.ConflictException;
import com.horizonbank.homebanking.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final int ACCOUNT_NUMBER_LENGTH = 22;
    private static final int MAX_NUMBER_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();

    private final AccountJpaRepository accounts;
    private final AccountBalanceJpaRepository balances;
    private final CurrencyJpaRepository currencies;

    @Transactional
    public AccountView create(CreateAccountCommand command) {
        if (command.type() == AccountType.SYSTEM) {
            throw new BusinessRuleException("Account type not allowed");
        }
        if (!currencies.existsById(command.currencyCode())) {
            throw new BusinessRuleException("Unsupported currency");
        }

        String alias = normalizeAlias(command.alias());
        if (alias != null && accounts.existsByAliasIgnoreCase(alias)) {
            throw new ConflictException("Alias already in use");
        }

        AccountEntity account = accounts.save(new AccountEntity(
                command.customerId(),
                generateAccountNumber(),
                alias,
                command.currencyCode(),
                command.type()));
        AccountBalanceEntity balance = balances.save(new AccountBalanceEntity(account.getId()));

        return toView(account, balance.getBalance());
    }

    @Transactional(readOnly = true)
    public List<AccountView> listByCustomer(UUID customerId) {
        List<AccountEntity> owned = accounts.findAllByCustomerIdOrderByCreatedAtAsc(customerId);
        Map<UUID, BigDecimal> balanceByAccount = balances
                .findAllById(owned.stream().map(AccountEntity::getId).toList())
                .stream()
                .collect(Collectors.toMap(AccountBalanceEntity::getAccountId,
                        AccountBalanceEntity::getBalance, (a, b) -> a, java.util.HashMap::new));
        return owned.stream()
                .map(account -> toView(account, balanceByAccount.getOrDefault(account.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountView getOwned(UUID customerId, UUID accountId) {
        AccountEntity account = accounts.findByIdAndCustomerId(accountId, customerId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        BigDecimal balance = balances.findById(accountId)
                .map(AccountBalanceEntity::getBalance)
                .orElse(BigDecimal.ZERO);
        return toView(account, balance);
    }

    private String generateAccountNumber() {
        for (int attempt = 0; attempt < MAX_NUMBER_ATTEMPTS; attempt++) {
            StringBuilder candidate = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
            candidate.append(1 + random.nextInt(9));
            for (int i = 1; i < ACCOUNT_NUMBER_LENGTH; i++) {
                candidate.append(random.nextInt(10));
            }
            if (!accounts.existsByAccountNumber(candidate.toString())) {
                return candidate.toString();
            }
        }
        throw new IllegalStateException("Could not generate a unique account number");
    }

    private String normalizeAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            return null;
        }
        return alias.trim().toLowerCase();
    }

    private AccountView toView(AccountEntity account, BigDecimal balance) {
        return new AccountView(
                account.getId(),
                account.getAccountNumber(),
                account.getAlias(),
                account.getCurrencyCode(),
                account.getType(),
                account.getStatus(),
                balance,
                account.getCreatedAt());
    }
}