package com.example.demo.service.risk;

import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class VelocityTx10mGt5Rule implements RiskRule {

	@Override
	public Optional<RiskHit> evaluate(RiskContext context) {
		if (context.transactionCountLast10m() > 5) {
			return Optional.of(new RiskHit("VELOCITY_TX_10M_GT_5", 25, "More than 5 transactions in the last 10 minutes"));
		}
		return Optional.empty();
	}
}