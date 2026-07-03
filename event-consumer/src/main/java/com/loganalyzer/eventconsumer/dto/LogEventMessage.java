package com.loganalyzer.eventconsumer.dto;

import java.time.Instant;

public record LogEventMessage(
		String id,
		String serviceName,
		String level,
		String message,
		Instant createdAt
) {
}
