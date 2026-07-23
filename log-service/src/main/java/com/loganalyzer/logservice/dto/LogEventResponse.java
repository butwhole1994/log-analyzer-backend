package com.loganalyzer.logservice.dto;

import java.time.Instant;

/**
 * 로그 이벤트 발행 성공 후 클라이언트에 반환하는 응답 본문이다.
 *
 * @param id 발행된 로그 이벤트 고유 식별자
 * @param topic 로그 이벤트가 발행된 Kafka 토픽명
 * @param traceId 응답과 연결된 요청 흐름 추적 식별자
 * @param requestId 응답과 연결된 단일 요청 식별자
 * @param timestamp 로그 이벤트 기준 시각
 * @author butwhole1994
 */
public record LogEventResponse(
		String id,
		String topic,
		String traceId,
		String requestId,
		Instant timestamp
) {
}
