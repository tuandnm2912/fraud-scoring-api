package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionCreateRequest(
		@NotNull @Positive BigDecimal amount,
		@NotBlank String currency,
		@NotBlank String country,
		@NotBlank String ip
) {
}