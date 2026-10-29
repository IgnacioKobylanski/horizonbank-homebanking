package com.horizonbank.homebanking.accounts.web;

import com.horizonbank.homebanking.accounts.application.AccountService;
import com.horizonbank.homebanking.accounts.application.AccountView;
import com.horizonbank.homebanking.accounts.application.CreateAccountCommand;
import com.horizonbank.homebanking.shared.error.UnauthorizedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody CreateAccountRequest request) {
        AccountView view = accountService.create(new CreateAccountCommand(
                customerId(jwt), request.currency(), request.type(), request.alias()));
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + view.id()))
                .body(AccountResponse.from(view));
    }

    @GetMapping
    public List<AccountResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return accountService.listByCustomer(customerId(jwt)).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public AccountResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return AccountResponse.from(accountService.getOwned(customerId(jwt), id));
    }

    private UUID customerId(Jwt jwt) {
        String claim = jwt.getClaimAsString("customer_id");
        if (claim == null) {
            throw new UnauthorizedException("Invalid token");
        }
        return UUID.fromString(claim);
    }
}