package com.horizonbank.homebanking.identity.web;

import com.horizonbank.homebanking.identity.application.RegisterUserCommand;
import com.horizonbank.homebanking.identity.domain.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull DocumentType documentType,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9]{6,20}$",
                message = "must be 6 to 20 letters or digits") String documentNumber,
        @Size(max = 30) String phone,
        @NotNull @Past LocalDate birthDate,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 72,
                message = "must be between 12 and 72 characters") String password
) {

    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(firstName, lastName, documentType, documentNumber,
                phone, birthDate, email, password);
    }
}