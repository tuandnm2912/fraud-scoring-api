package com.example.demo.service;

import com.example.demo.dto.RiskReasonResponse;
import com.example.demo.dto.RiskScoreResponse;
import com.example.demo.entity.RiskAssessment;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.RiskAssessmentRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.risk.RiskContext;
import com.example.demo.service.risk.RiskHit;
import com.example.demo.service.risk.RiskRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskScoringService {

	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;
	private final RiskAssessmentRepository riskAssessmentRepository;
	private final AuditLogService auditLogService;
	private final List<RiskRule> riskRules;
	private final ObjectMapper objectMapper;

	@Transactional
	public RiskScoreResponse score(Long transactionId, String email, String ipAddress, String userAgent) {
		User user = userRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		TransactionEntity transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
		if (!transaction.getUser().getId().equals(user.getId())) {
			throw new AccessDeniedException("You can only score your own transaction");
		}

		Instant evaluationTime = transaction.getCreatedAt();
		Instant since10m = evaluationTime.minus(Duration.ofMinutes(10));
		Instant since24h = evaluationTime.minus(Duration.ofHours(24));
		TransactionEntity previousTransaction = transactionRepository.findTopByUserAndCreatedAtBeforeOrderByCreatedAtDesc(user, evaluationTime).orElse(null);
		long txCount10m = transactionRepository.countByUserAndCreatedAtAfter(user, since10m);
		long txCount24h = transactionRepository.countByUserAndCreatedAtAfter(user, since24h);
		BigDecimal totalAmount24h = transactionRepository.sumAmountSince(user, since24h);
		if (totalAmount24h == null) {
			totalAmount24h = BigDecimal.ZERO;
		}

		RiskContext context = new RiskContext(transaction, user, previousTransaction, txCount10m, txCount24h, totalAmount24h, evaluationTime);
		List<RiskHit> hits = new ArrayList<>();
		for (RiskRule riskRule : riskRules) {
			riskRule.evaluate(context).ifPresent(hits::add);
		}

		int score = Math.min(100, hits.stream().mapToInt(RiskHit::points).sum());
		String level = resolveLevel(score);
		String recommendation = resolveRecommendation(level);
		List<RiskReasonResponse> reasons = hits.stream()
				.map(hit -> new RiskReasonResponse(hit.reasonCode(), hit.points(), hit.description()))
				.toList();

		RiskAssessment assessment = new RiskAssessment();
		assessment.setTransaction(transaction);
		assessment.setUser(user);
		assessment.setScore(score);
		assessment.setLevel(level);
		assessment.setRecommendation(recommendation);
		assessment.setReasonsJson(writeReasons(reasons));
		assessment.setCreatedAt(Instant.now());
		assessment = riskAssessmentRepository.save(assessment);

		auditLogService.log(user, "RISK_SCORE", "SUCCESS", "Scored transaction " + transactionId + " with level " + level + " and score " + score, ipAddress, userAgent);
		return new RiskScoreResponse(assessment.getId(), transaction.getId(), score, level, reasons, recommendation, assessment.getCreatedAt());
	}

	private String resolveLevel(int score) {
		if (score >= 70) {
			return "HIGH";
		}
		if (score >= 30) {
			return "MEDIUM";
		}
		return "LOW";
	}

	private String resolveRecommendation(String level) {
		return switch (level) {
			case "HIGH" -> "Hold the transaction for manual review and possible investigation.";
			case "MEDIUM" -> "Review the transaction before approval.";
			default -> "Proceed with standard monitoring.";
		};
	}

	private String writeReasons(List<RiskReasonResponse> reasons) {
		try {
			return objectMapper.writeValueAsString(reasons);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Unable to serialize risk reasons", exception);
		}
	}
}