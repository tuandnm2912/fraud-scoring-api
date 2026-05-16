package com.example.demo.controller;

import com.example.demo.dto.RiskScoreRequest;
import com.example.demo.dto.RiskScoreResponse;
import com.example.demo.service.RiskScoringService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/risk")
public class RiskController {

	private final RiskScoringService riskScoringService;

	@PostMapping("/score")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<RiskScoreResponse> score(
			@Valid @RequestBody RiskScoreRequest request,
			Principal principal,
			HttpServletRequest httpServletRequest
	) {
		return ResponseEntity.ok(riskScoringService.score(request.transactionId(), principal.getName(), clientIp(httpServletRequest), httpServletRequest.getHeader("User-Agent")));
	}

	private String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}