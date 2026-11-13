package com.horizonbank.homebanking;

import com.horizonbank.homebanking.accounts.application.AccountService;
import com.horizonbank.homebanking.accounts.application.AccountView;
import com.horizonbank.homebanking.accounts.application.CreateAccountCommand;
import com.horizonbank.homebanking.accounts.domain.AccountType;
import com.horizonbank.homebanking.accounts.infrastructure.persistence.AccountBalanceJpaRepository;
import com.horizonbank.homebanking.identity.application.RegisterUserCommand;
import com.horizonbank.homebanking.identity.application.RegisterUserService;
import com.horizonbank.homebanking.identity.application.RegisteredUser;
import com.horizonbank.homebanking.identity.domain.DocumentType;
import com.horizonbank.homebanking.ledger.application.DepositReceipt;
import com.horizonbank.homebanking.ledger.application.LedgerService;
import com.horizonbank.homebanking.ledger.domain.EntryDirection;
import com.horizonbank.homebanking.ledger.infrastructure.persistence.LedgerEntryEntity;
import com.horizonbank.homebanking.ledger.infrastructure.persistence.LedgerEntryJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class DepositConcurrencyTest {

	private static final UUID SYSTEM_CASH_ARS = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final int THREADS = 32;
	private static final int DEPOSITS = 200;
	private static final BigDecimal AMOUNT = new BigDecimal("10.00");

	@Autowired
	private RegisterUserService registerUserService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private LedgerService ledgerService;
	@Autowired
	private AccountBalanceJpaRepository balances;
	@Autowired
	private LedgerEntryJpaRepository entries;

	@Test
	void concurrentDepositsKeepBalancesExact() throws Exception {
		String unique = UUID.randomUUID().toString().replace("-", "");
		RegisteredUser user = registerUserService.register(new RegisterUserCommand(
				"Test", "Concurrency", DocumentType.DNI, unique.substring(0, 12), null,
				LocalDate.of(1990, 1, 1), unique + "@example.com", "una-clave-segura-123"));
		AccountView account = accountService.create(
				new CreateAccountCommand(user.customerId(), "ARS", AccountType.SAVINGS, null));
		BigDecimal cashBefore = systemCashBalance();

		ExecutorService pool = Executors.newFixedThreadPool(THREADS);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<DepositReceipt>> futures = new ArrayList<>();
		for (int i = 0; i < DEPOSITS; i++) {
			futures.add(pool.submit(() -> {
				start.await();
				return ledgerService.deposit(user.userId(), user.customerId(), account.id(), AMOUNT);
			}));
		}
		start.countDown();
		for (Future<DepositReceipt> future : futures) {
			future.get(60, TimeUnit.SECONDS);
		}
		pool.shutdown();

		BigDecimal expected = AMOUNT.multiply(BigDecimal.valueOf(DEPOSITS));

		BigDecimal stored = accountService.getOwned(user.customerId(), account.id()).balance();
		assertThat(stored).isEqualByComparingTo(expected);

		List<LedgerEntryEntity> accountEntries = entries.findAll().stream()
				.filter(entry -> entry.getAccountId().equals(account.id()))
				.toList();
		BigDecimal fromLedger = accountEntries.stream()
				.map(entry -> entry.getDirection() == EntryDirection.CREDIT
						? entry.getAmount() : entry.getAmount().negate())
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		assertThat(accountEntries).hasSize(DEPOSITS);
		assertThat(fromLedger).isEqualByComparingTo(expected);

		assertThat(systemCashBalance().subtract(cashBefore)).isEqualByComparingTo(expected.negate());
	}

	private BigDecimal systemCashBalance() {
		return balances.findById(SYSTEM_CASH_ARS).orElseThrow().getBalance();
	}
}