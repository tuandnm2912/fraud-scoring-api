package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record RiskScoreRequest(@NotNull Long transactionId) {
}