package com.example.demo.service.risk;

import java.time.ZoneId;

import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class NightTimeRule implements RiskRule {

	@Override
	public Optional<RiskHit> evaluate(RiskContext context) {
		int hour = context.evaluationTime().atZone(ZoneId.systemDefault()).getHour();
		if (hour >= 22 || hour < 6) {
			return Optional.of(new RiskHit("NIGHT_TIME", 10, "Transaction occurred during night hours"));
		}
		return Optional.empty();
	}
}