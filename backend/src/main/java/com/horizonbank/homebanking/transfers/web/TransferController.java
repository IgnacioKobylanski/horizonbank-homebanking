package com.horizonbank.homebanking.transfers.web;

import com.horizonbank.homebanking.shared.error.BusinessRuleException;
import com.horizonbank.homebanking.shared.security.AuthenticatedCustomer;
import com.horizonbank.homebanking.transfers.application.TransferCommand;
import com.horizonbank.homebanking.transfers.application.TransferResult;
import com.horizonbank.homebanking.transfers.application.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestHeader("Idempotency-Key") String idempotencyKey,
                                                   @Valid @RequestBody TransferRequest request) {
        String key = idempotencyKey.trim();
        if (key.length() < 8 || key.length() > 100) {
            throw new BusinessRuleException("Idempotency-Key must be between 8 and 100 characters");
        }

        TransferResult result = transferService.transfer(new TransferCommand(
                AuthenticatedCustomer.userIdFrom(jwt),
                AuthenticatedCustomer.idFrom(jwt),
                key,
                request.sourceAccountId(),
                request.destination(),
                request.amount(),
                request.concept()));

        TransferResponse body = TransferResponse.from(result.transfer());
        if (result.replayed()) {
            return ResponseEntity.ok().header("Idempotent-Replayed", "true").body(body);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}