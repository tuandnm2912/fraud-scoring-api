package com.example.demo.service.risk;

import java.util.Optional;

public interface RiskRule {

	Optional<RiskHit> evaluate(RiskContext context);
}