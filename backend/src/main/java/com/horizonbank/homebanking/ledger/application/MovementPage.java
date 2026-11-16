package com.horizonbank.homebanking.ledger.application;

import java.util.List;

public record MovementPage(List<MovementView> items, Long nextCursor) {
}