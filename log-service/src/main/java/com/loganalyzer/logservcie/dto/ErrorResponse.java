package com.loganalyzer.logservcie.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
		Instant timestamp,
		String path,
		String code,
		String message,
		List<ErrorDetail> details
) {
}
