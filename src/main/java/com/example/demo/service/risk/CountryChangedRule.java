package com.example.demo.service.risk;

import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
public class CountryChangedRule implements RiskRule {

	@Override
	public Optional<RiskHit> evaluate(RiskContext context) {
		if (context.previousTransaction() != null) {
			String previousCountry = context.previousTransaction().getCountry();
			String currentCountry = context.transaction().getCountry();
			if (previousCountry != null && !previousCountry.equalsIgnoreCase(currentCountry)) {
				return Optional.of(new RiskHit("COUNTRY_CHANGED", 15, "Transaction country changed from the previous transaction"));
			}
		}
		return Optional.empty();
	}
}