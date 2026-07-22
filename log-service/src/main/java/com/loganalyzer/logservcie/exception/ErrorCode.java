package com.loganalyzer.logservcie.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
	MALFORMED_JSON(HttpStatus.BAD_REQUEST, "Request body is malformed or unreadable"),
	KAFKA_PUBLISH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Failed to publish log event"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String message() {
		return message;
	}
}
