package com.horizonbank.homebanking.ledger.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount
) {
}