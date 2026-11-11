package com.horizonbank.homebanking.transfers.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceAccountId,
        @NotBlank @Size(max = 30) String destination,
        @NotNull @DecimalMin("0.01") @Digits(integer = 15, fraction = 2) BigDecimal amount,
        @Size(max = 140) String concept
) {
}