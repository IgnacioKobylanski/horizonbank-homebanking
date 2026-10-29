package com.horizonbank.homebanking.accounts.web;

import com.horizonbank.homebanking.accounts.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$", message = "must be a 3-letter currency code") String currency,
        @NotNull AccountType type,
        @Pattern(regexp = "^[A-Za-z0-9.]{6,30}$",
                message = "must be 6 to 30 letters, digits or dots") String alias
) {
}