package com.loganalyzer.logservcie.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * log-service가 Kafka로 발행하는 이벤트 DTO다.
 *
 * <p>이 타입은 전송 경계에서 사용하는 메시지 포맷이며,
 * consumer와 OpenSearch 문서가 그대로 해석할 수 있도록 구성한다.
 *
 * @author butwhole1994
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogEventMessage(
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
