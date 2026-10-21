package com.horizonbank.homebanking.identity.application;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
}