package com.horizonbank.homebanking.ledger.web;

import com.horizonbank.homebanking.ledger.application.LedgerService;
import com.horizonbank.homebanking.shared.security.AuthenticatedCustomer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final LedgerService ledgerService;

    @PostMapping
    public ResponseEntity<DepositResponse> deposit(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID accountId,
                                                   @Valid @RequestBody DepositRequest request) {
        DepositResponse body = DepositResponse.from(
                ledgerService.deposit(
                        AuthenticatedCustomer.userIdFrom(jwt),
                        AuthenticatedCustomer.idFrom(jwt),
                        accountId,
                        request.amount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}