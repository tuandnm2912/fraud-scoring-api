package com.example.demo.dto;

public record ApiErrorResponse(String code, String message, Object details, String traceId) {
}