package com.example.demo.service.risk;

import java.time.Duration;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class NewAccountLt3DaysRule implements RiskRule {

	@Override
	public Optional<RiskHit> evaluate(RiskContext context) {
		long accountAgeDays = Duration.between(context.user().getCreatedAt(), context.evaluationTime()).toDays();
		if (accountAgeDays < 3) {
			return Optional.of(new RiskHit("NEW_ACCOUNT_LT_3_DAYS", 20, "User account is newer than 3 days"));
		}
		return Optional.empty();
	}
}