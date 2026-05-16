package com.example.demo.service.risk;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AmountGt10MRule implements RiskRule {

	private static final BigDecimal THRESHOLD = new BigDecimal("10000000");

	@Override
	public Optional<RiskHit> evaluate(RiskContext context) {
		if (context.transaction().getAmount().compareTo(THRESHOLD) > 0) {
			return Optional.of(new RiskHit("AMOUNT_GT_10M", 30, "Transaction amount is above 10,000,000"));
		}
		return Optional.empty();
	}
}