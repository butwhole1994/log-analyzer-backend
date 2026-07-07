package com.log_analyzer.log_servcie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservcie.dto.LogEventRequest;
import com.loganalyzer.logservcie.dto.LogEventMessage;
import com.loganalyzer.logservcie.service.LogEventProducer;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class LogEventProducerTest {

	private final KafkaTemplate<String, String> kafkaTemplate = org.mockito.Mockito.mock(KafkaTemplate.class);
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final LogEventProducer producer = new LogEventProducer(kafkaTemplate, objectMapper);

	@Test
	void publish_sendsEventToInfraTopic() {
		ReflectionTestUtils.setField(producer, "serviceName", "log-service");
		ReflectionTestUtils.setField(producer, "logEventsTopic", "mvp.log-events");

		var response = producer.publish(new LogEventRequest(
				"hello",
				"info",
				"",
				"",
				"",
				"GET",
				"/health",
				200,
				12L,
				Instant.parse("2026-07-03T04:59:03Z"),
				"trace-1",
				"span-1"
		));
		var payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);

		assertThat(response.topic()).isEqualTo("mvp.log-events");
		assertThat(response.id()).isNotBlank();
		assertThat(response.timestamp()).isEqualTo(Instant.parse("2026-07-03T04:59:03Z"));
		verify(kafkaTemplate).send(eq("mvp.log-events"), eq(response.id()), payloadCaptor.capture());

		LogEventMessage message = org.junit.jupiter.api.Assertions.assertDoesNotThrow(
				() -> objectMapper.readValue(payloadCaptor.getValue(), LogEventMessage.class)
		);
		assertThat(message.id()).isEqualTo(response.id());
		assertThat(message.service()).isEqualTo("log-service");
		assertThat(message.level()).isEqualTo("INFO");
		assertThat(message.loggerName()).isEqualTo("log-service");
		assertThat(message.threadName()).isEqualTo(Thread.currentThread().getName());
		assertThat(message.message()).isEqualTo("hello");
		assertThat(message.traceId()).isEqualTo("trace-1");
		assertThat(message.spanId()).isEqualTo("span-1");
		assertThat(message.host()).isBlank();
		assertThat(message.method()).isEqualTo("GET");
		assertThat(message.path()).isEqualTo("/health");
		assertThat(message.statusCode()).isEqualTo(200);
		assertThat(message.durationMs()).isEqualTo(12L);
		assertThat(message.timestamp()).isEqualTo(response.timestamp());
	}
}
