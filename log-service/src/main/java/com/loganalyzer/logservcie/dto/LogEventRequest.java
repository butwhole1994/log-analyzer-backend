package com.loganalyzer.logservcie.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * log-service API가 받는 입력 DTO다.
 *
 * <p>이 타입은 사용자가 보내는 로그 입력 포맷이며,
 * Kafka/OpenSearch 공통 문서 포맷과는 분리되어 있다.
 *
 * @author butwhole1994
 */
public record LogEventRequest(
		// 로그 본문
		@NotBlank String message,
		// 로그 레벨
		String level,
		// 로그를 기록한 로거명
		String loggerName,
		// 로그를 기록한 스레드명
		String threadName,
		// 로그가 실행된 호스트명
		String host,
		// HTTP 메서드
		String method,
		// HTTP 요청 경로
		String path,
		// HTTP 응답 상태 코드
		Integer statusCode,
		// 요청 처리 시간
		Long durationMs,
		// 로그가 발생한 시각
		Instant timestamp,
		// 추적용 트레이스 ID
		String traceId,
		// 추적용 스팬 ID
		String spanId
) {
}
