package com.loganalyzer.eventconsumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

/**
 * mvp.log-events 토픽에서 수신하는 로그 이벤트 메시지 형식이다.
 *
 * @param eventId 로그 이벤트 고유 식별자
 * @param serviceName 로그를 발생시킨 서비스명
 * @param level 로그 레벨
 * @param loggerName 로거 이름
 * @param threadName 로그가 발생한 스레드 이름
 * @param message 로그 메시지 본문
 * @param traceId 요청 흐름 추적 식별자
 * @param requestId 단일 요청 식별자
 * @param spanId 분산 추적 구간 식별자
 * @param host 요청 호스트
 * @param method HTTP 메서드
 * @param path 요청 경로
 * @param statusCode HTTP 응답 상태 코드
 * @param durationMs 요청 처리 시간 밀리초
 * @param timestamp 로그 발생 시각
 * @param metadata 로그와 함께 전달되는 확장 메타데이터
 * @author butwhole1994
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogEventMessage(
		@JsonProperty("id")
		String eventId,

		@JsonProperty("service")
		String serviceName,

		String level,
		String loggerName,
		String threadName,
		String message,
		String traceId,
		String requestId,
		String spanId,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs,
		Instant timestamp,
		Map<String, Object> metadata
) {
}
