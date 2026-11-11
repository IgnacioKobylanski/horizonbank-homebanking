package com.horizonbank.homebanking.transfers.application;

public record TransferResult(TransferView transfer, boolean replayed) {
}