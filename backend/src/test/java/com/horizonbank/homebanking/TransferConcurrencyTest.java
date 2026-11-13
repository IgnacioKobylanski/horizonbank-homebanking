package com.horizonbank.homebanking;

import com.horizonbank.homebanking.accounts.application.AccountService;
import com.horizonbank.homebanking.accounts.application.AccountView;
import com.horizonbank.homebanking.accounts.application.CreateAccountCommand;
import com.horizonbank.homebanking.accounts.domain.AccountType;
import com.horizonbank.homebanking.identity.application.RegisterUserCommand;
import com.horizonbank.homebanking.identity.application.RegisterUserService;
import com.horizonbank.homebanking.identity.application.RegisteredUser;
import com.horizonbank.homebanking.identity.domain.DocumentType;
import com.horizonbank.homebanking.ledger.application.LedgerService;
import com.horizonbank.homebanking.transfers.application.TransferCommand;
import com.horizonbank.homebanking.transfers.application.TransferResult;
import com.horizonbank.homebanking.transfers.application.TransferService;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class TransferConcurrencyTest {

    private static final int THREADS = 32;

    @Autowired
    private RegisterUserService registerUserService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LedgerService ledgerService;
    @Autowired
    private TransferService transferService;

    private record Holder(UUID userId, UUID customerId, UUID accountId, String accountNumber) {
    }

    @Test
    void crossTransfersDoNotDeadlockAndConserveMoney() throws Exception {
        Holder a = newHolder("1000.00");
        Holder b = newHolder("1000.00");

        List<Callable<TransferResult>> tasks = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            tasks.add(() -> transfer(a, b, "3.00", UUID.randomUUID().toString()));
            tasks.add(() -> transfer(b, a, "2.00", UUID.randomUUID().toString()));
        }
        runConcurrently(tasks);

        // A sent 100 x 3.00 and received 100 x 2.00; B the opposite.
        assertThat(balanceOf(a)).isEqualByComparingTo("900.00");
        assertThat(balanceOf(b)).isEqualByComparingTo("1100.00");
        assertThat(balanceOf(a).add(balanceOf(b))).isEqualByComparingTo("2000.00");
    }

    @Test
    void sameIdempotencyKeyMovesMoneyOnlyOnce() throws Exception {
        Holder a = newHolder("500.00");
        Holder b = newHolder("0.00");
        String key = UUID.randomUUID().toString();

        List<Callable<TransferResult>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tasks.add(() -> transfer(a, b, "100.00", key));
        }
        List<TransferResult> results = runConcurrently(tasks);

        long created = results.stream().filter(result -> !result.replayed()).count();
        long distinctTransfers = results.stream().map(result -> result.transfer().id()).distinct().count();
        assertThat(created).isEqualTo(1);
        assertThat(distinctTransfers).isEqualTo(1);
        assertThat(balanceOf(a)).isEqualByComparingTo("400.00");
        assertThat(balanceOf(b)).isEqualByComparingTo("100.00");
    }

    private Holder newHolder(String initialDeposit) {
        String unique = UUID.randomUUID().toString().replace("-", "");
        RegisteredUser user = registerUserService.register(new RegisterUserCommand(
                "Test", "Transfers", DocumentType.DNI, unique.substring(0, 12), null,
                LocalDate.of(1990, 1, 1), unique + "@example.com", "una-clave-segura-123"));
        AccountView account = accountService.create(
                new CreateAccountCommand(user.customerId(), "ARS", AccountType.SAVINGS, null));
        BigDecimal amount = new BigDecimal(initialDeposit);
        if (amount.signum() > 0) {
            ledgerService.deposit(user.userId(), user.customerId(), account.id(), amount);
        }
        return new Holder(user.userId(), user.customerId(), account.id(), account.accountNumber());
    }

    private TransferResult transfer(Holder from, Holder to, String amount, String key) {
        return transferService.transfer(new TransferCommand(
                from.userId(), from.customerId(), key, from.accountId(),
                to.accountNumber(), new BigDecimal(amount), null));
    }

    private BigDecimal balanceOf(Holder holder) {
        return accountService.getOwned(holder.customerId(), holder.accountId()).balance();
    }

    private <T> List<T> runConcurrently(List<Callable<T>> tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return task.call();
                }));
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(60, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }
}