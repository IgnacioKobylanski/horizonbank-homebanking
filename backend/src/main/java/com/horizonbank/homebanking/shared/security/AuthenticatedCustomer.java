package com.horizonbank.homebanking.shared.security;

import com.horizonbank.homebanking.shared.error.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public final class AuthenticatedCustomer {

    private AuthenticatedCustomer() {
    }

    public static UUID idFrom(Jwt jwt) {
        String claim = jwt.getClaimAsString("customer_id");
        if (claim == null) {
            throw new UnauthorizedException("Invalid token");
        }
        return UUID.fromString(claim);
    }
}