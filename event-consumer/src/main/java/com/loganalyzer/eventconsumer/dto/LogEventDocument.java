package com.loganalyzer.eventconsumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * OpenSearch에 저장되는 로그 문서 포맷이다.
 *
 * <p>Kafka 이벤트와 거의 동일하지만, 저장소에서 조회하기 좋은 형태로 유지한다.
 *
 * @author butwhole1994
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogEventDocument(
		// 로그 이벤트 식별자
		String id,
		// 로그를 생성한 서비스명
		String service,
		// 로그 레벨
		String level,
		// 로그를 기록한 로거명
		String loggerName,
		// 로그를 기록한 스레드명
		String threadName,
		// 로그 본문
		String message,
		// 추적용 트레이스 ID
		String traceId,
		// 추적용 스팬 ID
		String spanId,
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
		Instant timestamp
) {
}
