package com.loganalyzer.logservcie.dto;

public record ApiResponse<T>(
		boolean success,
		T data,
		ResponseMeta meta,
		ErrorResponse error
) {

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, null);
	}

	public static <T> ApiResponse<T> success(T data, ResponseMeta meta) {
		return new ApiResponse<>(true, data, meta, null);
	}

	public static <T> ApiResponse<T> fail(ErrorResponse error) {
		return new ApiResponse<>(false, null, null, error);
	}
}
