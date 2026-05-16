package com.example.demo.dto;

import java.time.Instant;

public record AuditLogResponse(
		Long id,
		Long userId,
		String userEmail,
		String action,
		String status,
		String details,
		String ipAddress,
		String userAgent,
		Instant createdAt
) {
}