package com.loganalyzer.logservcie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

/**
 * Request body accepted by log-service before publishing a log event to Kafka.
 */
public record LogEventRequest(
		@NotBlank(message = "serviceName is required")
		@Size(max = 100, message = "serviceName must be 100 characters or fewer")
		String serviceName,

		@NotBlank(message = "level is required")
		@Pattern(
				regexp = "TRACE|DEBUG|INFO|WARN|ERROR",
				message = "level must be one of TRACE, DEBUG, INFO, WARN, ERROR"
		)
		String level,

		@NotBlank(message = "message is required")
		@Size(max = 4000, message = "message must be 4000 characters or fewer")
		String message,

		@NotNull(message = "timestamp is required")
		@PastOrPresent(message = "timestamp must not be in the future")
		Instant timestamp,

		@Size(max = 100, message = "traceId must be 100 characters or fewer")
		String traceId,

		@Size(max = 100, message = "requestId must be 100 characters or fewer")
		String requestId,

		@Size(max = 50, message = "metadata must contain 50 entries or fewer")
		Map<String, Object> metadata,

		String loggerName,
		String threadName,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs,
		String spanId
) {
}
