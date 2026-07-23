package com.loganalyzer.eventconsumer.dto;

import java.time.Instant;

/**
 * Kafka 로그 이벤트 consume 상태를 확인하기 위한 API 응답 데이터다.
 *
 * @param consumedCount 애플리케이션 기동 이후 consume에 성공한 메시지 수
 * @param lastConsumedAt 마지막 consume 성공 시각
 * @param lastEvent 마지막으로 consume에 성공한 로그 이벤트 요약
 * @author butwhole1994
 */
public record LogEventConsumeStatusResponse(
		String topic,
		String consumerGroupId,
		long consumedCount,
		Instant lastConsumedAt,
		ConsumedLogEventSummary lastEvent
) {
}
