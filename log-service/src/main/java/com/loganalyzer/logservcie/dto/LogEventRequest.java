package com.loganalyzer.logservcie.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record LogEventRequest(
		@NotBlank String message,
		String level,
		String loggerName,
		String threadName,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs,
		Instant timestamp,
		String traceId,
		String spanId
) {
}
