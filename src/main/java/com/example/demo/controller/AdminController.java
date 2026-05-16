package com.example.demo.controller;

import com.example.demo.dto.AuditLogResponse;
import com.example.demo.entity.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

	private final AuditLogRepository auditLogRepository;

	@GetMapping("/audit-logs")
	@PreAuthorize("hasRole('ADMIN')")
	@Transactional(readOnly = true)
	public ResponseEntity<Page<AuditLogResponse>> auditLogs(
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) int size,
			@RequestParam(required = false) String action
	) {
		Pageable pageable = PageRequest.of(page, size);
		Page<AuditLog> logs = (action == null || action.isBlank())
				? auditLogRepository.findAll(pageable)
				: auditLogRepository.findByActionIgnoreCase(action.trim(), pageable);
		return ResponseEntity.ok(logs.map(this::toResponse));
	}

	private AuditLogResponse toResponse(AuditLog auditLog) {
		return new AuditLogResponse(
				auditLog.getId(),
				auditLog.getUser() == null ? null : auditLog.getUser().getId(),
				auditLog.getUser() == null ? null : auditLog.getUser().getEmail(),
				auditLog.getAction(),
				auditLog.getStatus(),
				auditLog.getDetails(),
				auditLog.getIpAddress(),
				auditLog.getUserAgent(),
				auditLog.getCreatedAt()
		);
	}
}