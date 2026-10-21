package com.horizonbank.homebanking.identity.web;

import com.horizonbank.homebanking.identity.application.TokenPair;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static TokenResponse from(TokenPair pair) {
        return new TokenResponse(pair.accessToken(), pair.refreshToken(), "Bearer", pair.expiresInSeconds());
    }
}