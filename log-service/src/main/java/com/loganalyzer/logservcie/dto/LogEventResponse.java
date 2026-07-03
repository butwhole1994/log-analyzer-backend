package com.loganalyzer.logservcie.dto;

import java.time.Instant;

public record LogEventResponse(
		String id,
		String topic,
		Instant createdAt
) {
}
