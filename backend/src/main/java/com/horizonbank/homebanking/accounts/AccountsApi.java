package com.horizonbank.homebanking.accounts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AccountsApi {

    record AccountRef(UUID id, String currencyCode) {
    }

    record Movement(UUID accountId, BigDecimal delta) {
    }

    AccountRef requireActiveOwned(UUID customerId, UUID accountId);

    AccountRef systemCashAccount(String currencyCode);

    AccountRef findActiveByNumberOrAlias(String identifier);

    Map<UUID, BigDecimal> applyMovements(List<Movement> movements);
}