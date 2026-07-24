package com.loganalyzer.logservice.exception;

import org.springframework.http.HttpStatus;

/**
 * log-service에서 클라이언트에 노출하는 표준 오류 코드와 HTTP 상태 매핑이다.
 *
 * @author butwhole1994
 */
public enum ErrorCode {
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Request validation failed"),
	MALFORMED_JSON(HttpStatus.BAD_REQUEST, "Request body is malformed or unreadable"),
	KAFKA_PUBLISH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Failed to publish log event"),
	OPENSEARCH_SEARCH_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "Failed to search logs"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");

	private final HttpStatus status;
	private final String message;

	/**
	 * 오류 코드별 HTTP 상태와 기본 메시지를 초기화한다.
	 *
	 * @param status 응답에 사용할 HTTP 상태
	 * @param message 응답에 사용할 기본 오류 메시지
	 */
	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	/**
	 * 오류 코드에 대응하는 HTTP 상태를 반환한다.
	 *
	 * @return HTTP 상태
	 */
	public HttpStatus status() {
		return status;
	}

	/**
	 * 오류 코드에 대응하는 기본 메시지를 반환한다.
	 *
	 * @return 기본 오류 메시지
	 */
	public String message() {
		return message;
	}
}
