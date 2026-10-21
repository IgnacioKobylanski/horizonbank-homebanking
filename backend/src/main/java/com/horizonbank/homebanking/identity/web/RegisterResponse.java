package com.horizonbank.homebanking.identity.web;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email) {
}