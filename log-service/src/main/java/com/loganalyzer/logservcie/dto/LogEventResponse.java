package com.loganalyzer.logservcie.dto;

import java.time.Instant;

/**
 * 로그 발행 결과를 돌려주는 응답 DTO다.
 *
 * @author butwhole1994
 */
public record LogEventResponse(
		// 로그 이벤트 식별자
		String id,
		// 발행된 Kafka 토픽명
		String topic,
		// 로그가 발생한 시각
		Instant timestamp
) {
}
