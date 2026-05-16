package com.example.demo.dto;

import java.time.Instant;
import java.util.List;

public record RiskScoreResponse(
		Long riskAssessmentId,
		Long transactionId,
		int score,
		String level,
		List<RiskReasonResponse> reasons,
		String recommendation,
		Instant createdAt
) {
}