package com.horizonbank.homebanking.ledger.application;

import com.horizonbank.homebanking.accounts.AccountsApi;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovementQueryService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private static final String SQL = """
            select * from (
                select e.id, e.transaction_id, t.type, t.description, e.direction, e.amount, e.created_at,
                       sum(case e.direction when 'CREDIT' then e.amount else -e.amount end)
                           over (order by e.id) as balance_after
                from ledger_entries e
                join ledger_transactions t on t.id = e.transaction_id
                where e.account_id = :accountId
            ) x
            where (:cursor is null or x.id < :cursor)
            order by x.id desc
            limit :limit
            """;

    private final AccountsApi accountsApi;
    private final JdbcClient jdbc;

    @Transactional(readOnly = true)
    public MovementPage list(UUID customerId, UUID accountId, Integer requestedLimit, Long cursor) {
        accountsApi.requireOwned(customerId, accountId);
        int limit = requestedLimit == null
                ? DEFAULT_LIMIT
                : Math.min(Math.max(requestedLimit, 1), MAX_LIMIT);

        List<MovementView> rows = jdbc.sql(SQL)
                .param("accountId", accountId)
                .param("cursor", cursor, Types.BIGINT)
                .param("limit", limit + 1)
                .query((rs, rowNum) -> new MovementView(
                        rs.getLong("id"),
                        rs.getObject("transaction_id", UUID.class),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("direction"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("balance_after"),
                        rs.getObject("created_at", OffsetDateTime.class).toInstant()))
                .list();

        boolean hasMore = rows.size() > limit;
        List<MovementView> items = List.copyOf(hasMore ? rows.subList(0, limit) : rows);
        Long nextCursor = hasMore ? items.get(items.size() - 1).id() : null;
        return new MovementPage(items, nextCursor);
    }
}