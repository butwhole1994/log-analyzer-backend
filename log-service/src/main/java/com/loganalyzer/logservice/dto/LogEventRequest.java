package com.loganalyzer.logservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Map;

/**
 * 로그 이벤트를 Kafka로 발행하기 전에 HTTP API에서 받는 요청 본문이다.
 *
 * @param serviceName 로그를 발생시킨 서비스명
 * @param level 로그 레벨
 * @param message 로그 메시지 본문
 * @param timestamp 로그 발생 시각
 * @param traceId 요청 흐름 추적 식별자
 * @param requestId 단일 요청 식별자
 * @param metadata 로그와 함께 전달되는 확장 메타데이터
 * @param loggerName 로거 이름
 * @param threadName 로그가 발생한 스레드 이름
 * @param host 요청 호스트
 * @param method HTTP 메서드
 * @param path 요청 경로
 * @param statusCode HTTP 응답 상태 코드
 * @param durationMs 요청 처리 시간 밀리초
 * @param spanId 분산 추적 구간 식별자
 * @author butwhole1994
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
