package com.example.demo.service.risk;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class Total24hGt100MRule implements RiskRule {

	private static final BigDecimal THRESHOLD = new BigDecimal("100000000");

	@Override
	public Optional<RiskHit> evaluate(RiskContext context) {
		if (context.totalAmountLast24h().compareTo(THRESHOLD) > 0) {
			return Optional.of(new RiskHit("TOTAL_24H_GT_100M", 20, "Total transaction amount in the last 24 hours is above 100,000,000"));
		}
		return Optional.empty();
	}
}