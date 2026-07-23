package com.loganalyzer.logservcie.dto;

public record ResponseMeta(
		Integer page,
		Integer size,
		Long totalElements,
		Integer totalPages,
		String sort
) {
}
