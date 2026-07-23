package com.loganalyzer.logservice.dto;

/**
 * log-service HTTP API가 공통으로 사용하는 응답 봉투다.
 *
 * @param success 요청 처리 성공 여부
 * @param data 성공 응답 데이터
 * @param meta 페이지네이션 등 부가 메타데이터
 * @param error 실패 시 표준 오류 응답
 * @param <T> 성공 응답 데이터 타입
 * @author butwhole1994
 */
public record ApiResponse<T>(
		boolean success,
		T data,
		ResponseMeta meta,
		ErrorResponse error
) {

	/**
	 * 메타데이터가 없는 성공 응답을 생성한다.
	 *
	 * @param data 성공 응답 데이터
	 * @param <T> 성공 응답 데이터 타입
	 * @return 표준 성공 응답
	 */
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null, null);
	}

	/**
	 * 메타데이터가 포함된 성공 응답을 생성한다.
	 *
	 * @param data 성공 응답 데이터
	 * @param meta 응답 메타데이터
	 * @param <T> 성공 응답 데이터 타입
	 * @return 표준 성공 응답
	 */
	public static <T> ApiResponse<T> success(T data, ResponseMeta meta) {
		return new ApiResponse<>(true, data, meta, null);
	}

	/**
	 * 표준 오류 응답을 포함한 실패 응답을 생성한다.
	 *
	 * @param error 오류 응답 본문
	 * @param <T> 성공 응답 데이터 타입
	 * @return 표준 실패 응답
	 */
	public static <T> ApiResponse<T> fail(ErrorResponse error) {
		return new ApiResponse<>(false, null, null, error);
	}
}
