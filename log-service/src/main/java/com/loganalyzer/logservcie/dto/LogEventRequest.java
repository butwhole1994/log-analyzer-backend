package com.loganalyzer.logservcie.dto;

import jakarta.validation.constraints.NotBlank;

public record LogEventRequest(
		@NotBlank String message,
		String level
) {
}
