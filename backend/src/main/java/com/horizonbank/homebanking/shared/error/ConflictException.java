package com.horizonbank.homebanking.shared.error;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}