package com.horizonbank.homebanking.identity.application;

import com.horizonbank.homebanking.identity.domain.DocumentType;

import java.time.LocalDate;

public record RegisterUserCommand(
        String firstName,
        String lastName,
        DocumentType documentType,
        String documentNumber,
        String phone,
        LocalDate birthDate,
        String email,
        String password
) {
}