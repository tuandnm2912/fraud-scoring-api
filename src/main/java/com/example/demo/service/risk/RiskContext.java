package com.example.demo.service.risk;

import com.example.demo.entity.TransactionEntity;
import com.example.demo.entity.User;
import java.math.BigDecimal;
import java.time.Instant;

public record RiskContext(
		TransactionEntity transaction,
		User user,
		TransactionEntity previousTransaction,
		long transactionCountLast10m,
		long transactionCountLast24h,
		BigDecimal totalAmountLast24h,
		Instant evaluationTime
) {
}