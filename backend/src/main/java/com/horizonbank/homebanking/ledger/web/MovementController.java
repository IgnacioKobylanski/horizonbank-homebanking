package com.horizonbank.homebanking.ledger.web;

import com.horizonbank.homebanking.ledger.application.MovementPage;
import com.horizonbank.homebanking.ledger.application.MovementQueryService;
import com.horizonbank.homebanking.shared.security.AuthenticatedCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/movements")
@RequiredArgsConstructor
public class MovementController {

    private final MovementQueryService movementQueryService;

    @GetMapping
    public MovementPage list(@AuthenticationPrincipal Jwt jwt,
                             @PathVariable UUID accountId,
                             @RequestParam(required = false) Integer limit,
                             @RequestParam(required = false) Long cursor) {
        return movementQueryService.list(AuthenticatedCustomer.idFrom(jwt), accountId, limit, cursor);
    }
}