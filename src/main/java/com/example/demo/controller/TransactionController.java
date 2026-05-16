package com.example.demo.controller;

import com.example.demo.dto.TransactionCreateRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<TransactionResponse> create(
			@Valid @RequestBody TransactionCreateRequest request,
			Principal principal,
			HttpServletRequest httpServletRequest
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request, principal.getName(), clientIp(httpServletRequest), httpServletRequest.getHeader("User-Agent")));
	}

	private String clientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}