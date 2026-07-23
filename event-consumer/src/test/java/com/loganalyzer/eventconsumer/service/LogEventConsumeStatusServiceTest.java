package com.loganalyzer.eventconsumer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.loganalyzer.eventconsumer.dto.LogEventConsumeStatusResponse;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * LogEventConsumeStatusService가 consume 성공 상태를 누적하고 마지막 이벤트를 보존하는지 검증한다.
 *
 * @author butwhole1994
 */
class LogEventConsumeStatusServiceTest {

	private static final Instant FIXED_NOW = Instant.parse("2026-07-03T05:00:00Z");

	private final LogEventConsumeStatusService statusService = new LogEventConsumeStatusService(
			Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
	);

	/**
	 * 초기 상태는 consume 성공 건수가 0이고 마지막 이벤트가 없어야 한다.
	 */
	@Test
	void getStatus_returnsEmptyStatusBeforeConsume() {
		LogEventConsumeStatusResponse status = statusService.getStatus();

		assertThat(status.topic()).isEqualTo("mvp.log-events");
		assertThat(status.consumerGroupId()).isEqualTo("event-consumer");
		assertThat(status.consumedCount()).isZero();
		assertThat(status.lastConsumedAt()).isNull();
		assertThat(status.lastEvent()).isNull();
	}

	/**
	 * consume 성공 메시지를 기록하면 누적 건수와 마지막 이벤트 요약이 갱신되는지 검증한다.
	 */
	@Test
	void recordConsumed_updatesCountAndLastEventSummary() {
		statusService.recordConsumed(message("event-1", "trace-1", "request-1"));
		statusService.recordConsumed(message("event-2", "trace-2", "request-2"));

		LogEventConsumeStatusResponse status = statusService.getStatus();

		assertThat(status.topic()).isEqualTo("mvp.log-events");
		assertThat(status.consumerGroupId()).isEqualTo("event-consumer");
		assertThat(status.consumedCount()).isEqualTo(2);
		assertThat(status.lastConsumedAt()).isEqualTo(FIXED_NOW);
		assertThat(status.lastEvent().eventId()).isEqualTo("event-2");
		assertThat(status.lastEvent().traceId()).isEqualTo("trace-2");
		assertThat(status.lastEvent().requestId()).isEqualTo("request-2");
		assertThat(status.lastEvent().serviceName()).isEqualTo("order-service");
		assertThat(status.lastEvent().level()).isEqualTo("INFO");
	}

	private LogEventMessage message(String eventId, String traceId, String requestId) {
		return new LogEventMessage(
				eventId,
				"order-service",
				"INFO",
				"com.loganalyzer.LogController",
				"main",
				"hello",
				traceId,
				requestId,
				"span-1",
				"localhost",
				"GET",
				"/health",
				200,
				12L,
				Instant.parse("2026-07-03T04:59:03Z"),
				Map.of("region", "ap-northeast-2")
		);
	}
}
