package com.horizonbank.homebanking.identity.application;

import com.horizonbank.homebanking.identity.infrastructure.persistence.CustomerEntity;
import com.horizonbank.homebanking.identity.infrastructure.persistence.CustomerJpaRepository;
import com.horizonbank.homebanking.identity.infrastructure.persistence.RoleEntity;
import com.horizonbank.homebanking.identity.infrastructure.persistence.RoleJpaRepository;
import com.horizonbank.homebanking.identity.infrastructure.persistence.UserEntity;
import com.horizonbank.homebanking.identity.infrastructure.persistence.UserJpaRepository;
import com.horizonbank.homebanking.shared.error.BusinessRuleException;
import com.horizonbank.homebanking.shared.error.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class RegisterUserService {

    private static final int MINIMUM_AGE = 18;
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final CustomerJpaRepository customers;
    private final UserJpaRepository users;
    private final RoleJpaRepository roles;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisteredUser register(RegisterUserCommand command) {
        if (Period.between(command.birthDate(), LocalDate.now()).getYears() < MINIMUM_AGE) {
            throw new BusinessRuleException("You must be at least " + MINIMUM_AGE + " years old");
        }

        String email = command.email().trim().toLowerCase();
        String documentNumber = command.documentNumber().trim().toUpperCase();

        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered");
        }
        if (customers.existsByDocumentTypeAndDocumentNumber(command.documentType(), documentNumber)) {
            throw new ConflictException("Document already registered");
        }

        RoleEntity customerRole = roles.findByName(CUSTOMER_ROLE)
                .orElseThrow(() -> new IllegalStateException("Role CUSTOMER not found"));

        CustomerEntity customer = customers.save(new CustomerEntity(
                command.firstName().trim(),
                command.lastName().trim(),
                command.documentType(),
                documentNumber,
                command.phone(),
                command.birthDate()));

        UserEntity user = new UserEntity(customer, email, passwordEncoder.encode(command.password()));
        user.addRole(customerRole);
        users.save(user);

        return new RegisteredUser(user.getId(), customer.getId(), email);
    }
}