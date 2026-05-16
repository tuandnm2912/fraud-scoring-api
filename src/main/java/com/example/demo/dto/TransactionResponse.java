package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(Long id, BigDecimal amount, String currency, String country, String ip, Instant createdAt) {
}