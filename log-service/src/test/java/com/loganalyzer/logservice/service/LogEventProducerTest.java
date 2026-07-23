package com.loganalyzer.logservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservice.dto.LogEventRequest;
import com.loganalyzer.logservice.dto.LogEventMessage;
import com.loganalyzer.logservice.service.LogEventProducer;
import com.loganalyzer.logservice.trace.TraceConstants;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * LogEventProducer가 요청 DTO를 Kafka 발행 메시지로 정확히 변환하는지 검증한다.
 *
 * @author butwhole1994
 */
class LogEventProducerTest {

	private final KafkaTemplate<String, String> kafkaTemplate = org.mockito.Mockito.mock(KafkaTemplate.class);
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final LogEventProducer producer = new LogEventProducer(kafkaTemplate, objectMapper);

	/**
	 * 테스트 간 MDC 추적 정보가 누수되지 않도록 정리한다.
	 */
	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	/**
	 * Kafka 토픽, 메시지 키, JSON payload, 응답 추적 정보가 기대값과 일치하는지 검증한다.
	 */
	@Test
	void publish_sendsEventToInfraTopic() {
		ReflectionTestUtils.setField(producer, "serviceName", "log-service");
		ReflectionTestUtils.setField(producer, "logEventsTopic", "mvp.log-events");
		MDC.put(TraceConstants.TRACE_ID_MDC_KEY, "trace-header-1");
		MDC.put(TraceConstants.REQUEST_ID_MDC_KEY, "request-header-1");
		CompletableFuture<SendResult<String, String>> sendResult = CompletableFuture.completedFuture(null);
		when(kafkaTemplate.send(eq("mvp.log-events"), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(sendResult);

		var response = producer.publish(new LogEventRequest(
				"order-service",
				"INFO",
				"hello",
				Instant.parse("2026-07-03T04:59:03Z"),
				"trace-body-1",
				"request-body-1",
				Map.of("region", "ap-northeast-2"),
				"",
				"",
				"",
				"GET",
				"/health",
				200,
				12L,
				"span-1"
		));
		var payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);

		assertThat(response.topic()).isEqualTo("mvp.log-events");
		assertThat(response.id()).isNotBlank();
		assertThat(response.traceId()).isEqualTo("trace-header-1");
		assertThat(response.requestId()).isEqualTo("request-header-1");
		assertThat(response.timestamp()).isEqualTo(Instant.parse("2026-07-03T04:59:03Z"));
		verify(kafkaTemplate).send(eq("mvp.log-events"), eq(response.id()), payloadCaptor.capture());

		LogEventMessage message = org.junit.jupiter.api.Assertions.assertDoesNotThrow(
				() -> objectMapper.readValue(payloadCaptor.getValue(), LogEventMessage.class)
		);
		assertThat(message.id()).isEqualTo(response.id());
		assertThat(message.service()).isEqualTo("order-service");
		assertThat(message.level()).isEqualTo("INFO");
		assertThat(message.loggerName()).isEqualTo("order-service");
		assertThat(message.threadName()).isEqualTo(Thread.currentThread().getName());
		assertThat(message.message()).isEqualTo("hello");
		assertThat(message.traceId()).isEqualTo("trace-header-1");
		assertThat(message.requestId()).isEqualTo("request-header-1");
		assertThat(message.spanId()).isEqualTo("span-1");
		assertThat(message.host()).isBlank();
		assertThat(message.method()).isEqualTo("GET");
		assertThat(message.path()).isEqualTo("/health");
		assertThat(message.statusCode()).isEqualTo(200);
		assertThat(message.durationMs()).isEqualTo(12L);
		assertThat(message.timestamp()).isEqualTo(response.timestamp());
		assertThat(message.metadata()).containsEntry("region", "ap-northeast-2");
	}
}
