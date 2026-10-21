package com.horizonbank.homebanking.identity.application;

import java.util.UUID;

public record RegisteredUser(UUID userId, UUID customerId, String email) {
}