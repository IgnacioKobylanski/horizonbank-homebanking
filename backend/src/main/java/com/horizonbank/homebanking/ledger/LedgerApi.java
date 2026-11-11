package com.horizonbank.homebanking.ledger;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerApi {

    UUID recordTransfer(UUID sourceAccountId, UUID destinationAccountId,
                        BigDecimal amount, String description);
}