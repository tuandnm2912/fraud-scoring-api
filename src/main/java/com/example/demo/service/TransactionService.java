package com.example.demo.service;

import com.example.demo.dto.TransactionCreateRequest;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.entity.TransactionEntity;
import com.example.demo.entity.User;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;
	private final AuditLogService auditLogService;

	@Transactional
	public TransactionResponse create(TransactionCreateRequest request, String email, String ipAddress, String userAgent) {
		User user = userRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new IllegalArgumentException("User not found"));

		TransactionEntity transaction = new TransactionEntity();
		transaction.setUser(user);
		transaction.setAmount(request.amount());
		transaction.setCurrency(request.currency().trim().toUpperCase());
		transaction.setCountry(request.country().trim().toUpperCase());
		transaction.setIpAddress(request.ip().trim());
		transaction.setCreatedAt(Instant.now());
		transaction = transactionRepository.save(transaction);

		auditLogService.log(user, "TX_CREATE", "SUCCESS", "Created transaction " + transaction.getId(), ipAddress, userAgent);
		return new TransactionResponse(transaction.getId(), transaction.getAmount(), transaction.getCurrency(), transaction.getCountry(), transaction.getIpAddress(), transaction.getCreatedAt());
	}
}