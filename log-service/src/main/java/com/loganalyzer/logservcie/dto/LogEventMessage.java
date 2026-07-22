package com.loganalyzer.logservcie.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Kafka payload published by log-service.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogEventMessage(
		String id,
		String service,
		String level,
		String loggerName,
		String threadName,
		String message,
		String traceId,
		String requestId,
		String spanId,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs,
		Instant timestamp,
		Map<String, Object> metadata
) {
}
