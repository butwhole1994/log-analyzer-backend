package com.loganalyzer.eventconsumer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.annotation.KafkaListener;

/**
 * LogEventConsumer가 Kafka payload를 내부 메시지로 변환하고 소비 성공 로그를 남기는지 검증한다.
 *
 * @author butwhole1994
 */
@ExtendWith(OutputCaptureExtension.class)
class LogEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final LogEventConsumeStatusService consumeStatusService = new LogEventConsumeStatusService(Clock.systemUTC());
	private final LogEventIndexService logEventIndexService = mock(LogEventIndexService.class);
	private final LogEventConsumer consumer = new LogEventConsumer(objectMapper, consumeStatusService, logEventIndexService);

	/**
	 * log-service가 발행한 Kafka payload가 LogEventMessage로 역직렬화되는지 검증한다.
	 *
	 * @throws Exception JSON fixture 생성 중 예외가 발생한 경우
	 */
	@Test
	void deserialize_convertsKafkaPayloadToLogEventMessage() throws Exception {
		String payload = objectMapper.writeValueAsString(Map.ofEntries(
				Map.entry("id", "event-1"),
				Map.entry("service", "order-service"),
				Map.entry("level", "INFO"),
				Map.entry("loggerName", "com.loganalyzer.LogController"),
				Map.entry("threadName", "main"),
				Map.entry("message", "hello"),
				Map.entry("traceId", "trace-1"),
				Map.entry("requestId", "request-1"),
				Map.entry("spanId", "span-1"),
				Map.entry("host", "localhost"),
				Map.entry("method", "GET"),
				Map.entry("path", "/health"),
				Map.entry("statusCode", 200),
				Map.entry("durationMs", 12L),
				Map.entry("timestamp", "2026-07-03T04:59:03Z"),
				Map.entry("metadata", Map.of("region", "ap-northeast-2"))
		));

		LogEventMessage message = consumer.deserialize(payload);

		assertThat(message.eventId()).isEqualTo("event-1");
		assertThat(message.serviceName()).isEqualTo("order-service");
		assertThat(message.level()).isEqualTo("INFO");
		assertThat(message.traceId()).isEqualTo("trace-1");
		assertThat(message.requestId()).isEqualTo("request-1");
		assertThat(message.timestamp()).isEqualTo(Instant.parse("2026-07-03T04:59:03Z"));
		assertThat(message.metadata()).containsEntry("region", "ap-northeast-2");
	}

	/**
	 * consume 성공 시 주요 식별자와 서비스 정보가 application log에 출력되는지 검증한다.
	 *
	 * @param output 테스트 중 캡처된 로그 출력
	 * @throws Exception JSON fixture 생성 중 예외가 발생한 경우
	 */
	@Test
	void consume_logsKeyFieldsWhenPayloadIsConsumed(CapturedOutput output) throws Exception {
		LogEventMessage message = new LogEventMessage(
				"event-1",
				"order-service",
				"INFO",
				"com.loganalyzer.LogController",
				"main",
				"hello",
				"trace-1",
				"request-1",
				"span-1",
				"localhost",
				"GET",
				"/health",
				200,
				12L,
				Instant.parse("2026-07-03T04:59:03Z"),
				Map.of("region", "ap-northeast-2")
		);
		String payload = objectMapper.writeValueAsString(message);

		assertDoesNotThrow(() -> consumer.consume(payload));

		assertThat(output).contains("Consumed log event successfully");
		assertThat(output).contains("eventId=event-1");
		assertThat(output).contains("traceId=trace-1");
		assertThat(output).contains("requestId=request-1");
		assertThat(output).contains("serviceName=order-service");
		assertThat(output).contains("level=INFO");
		verify(logEventIndexService).index(message);
		assertThat(consumeStatusService.getStatus().consumedCount()).isEqualTo(1);
		assertThat(consumeStatusService.getStatus().lastEvent().eventId()).isEqualTo("event-1");
	}

	/**
	 * Kafka listener가 요구된 topic과 consumer group 설정을 사용하는지 검증한다.
	 *
	 * @throws Exception consume 메서드 조회 실패 시 발생
	 */
	@Test
	void consume_usesLogEventsTopicAndEventConsumerGroup() throws Exception {
		Method method = LogEventConsumer.class.getMethod("consume", String.class);
		KafkaListener listener = method.getAnnotation(KafkaListener.class);

		assertThat(listener).isNotNull();
		assertThat(listener.topics()).containsExactly("${app.kafka.topics.log-events}");
		assertThat(listener.groupId()).isEqualTo("${app.kafka.consumer.group-id}");
	}
}
