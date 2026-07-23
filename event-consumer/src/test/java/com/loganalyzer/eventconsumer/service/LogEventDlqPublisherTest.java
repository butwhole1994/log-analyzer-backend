package com.loganalyzer.eventconsumer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class LogEventDlqPublisherTest {

	@SuppressWarnings("unchecked")
	private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final LogEventDlqPublisher publisher = new LogEventDlqPublisher(kafkaTemplate, objectMapper);

	@Test
	void publish_sendsDlqMessageWithTrackingFieldsAndFailureReason() throws Exception {
		ReflectionTestUtils.setField(publisher, "dlqTopic", "mvp.log-events.dlq");
		ReflectionTestUtils.setField(publisher, "consumerGroupId", "event-consumer");
		String originalPayload = objectMapper.writeValueAsString(Map.ofEntries(
				Map.entry("id", "event-1"),
				Map.entry("service", "order-service"),
				Map.entry("level", "ERROR"),
				Map.entry("message", "failed"),
				Map.entry("traceId", "trace-1"),
				Map.entry("requestId", "request-1"),
				Map.entry("timestamp", Instant.parse("2026-07-03T04:59:03Z").toString())
		));
		ConsumerRecord<String, String> record = new ConsumerRecord<>(
				"mvp.log-events",
				0,
				10L,
				"event-1",
				originalPayload
		);

		publisher.publish(record, new IllegalStateException("OpenSearch unavailable"));

		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(eq("mvp.log-events.dlq"), eq("event-1"), payloadCaptor.capture());
		JsonNode dlqPayload = objectMapper.readTree(payloadCaptor.getValue());
		assertThat(dlqPayload.get("eventId").asText()).isEqualTo("event-1");
		assertThat(dlqPayload.get("traceId").asText()).isEqualTo("trace-1");
		assertThat(dlqPayload.get("requestId").asText()).isEqualTo("request-1");
		assertThat(dlqPayload.get("sourceTopic").asText()).isEqualTo("mvp.log-events");
		assertThat(dlqPayload.get("sourcePartition").asInt()).isZero();
		assertThat(dlqPayload.get("sourceOffset").asLong()).isEqualTo(10L);
		assertThat(dlqPayload.get("consumerGroupId").asText()).isEqualTo("event-consumer");
		assertThat(dlqPayload.get("failureType").asText()).isEqualTo(IllegalStateException.class.getName());
		assertThat(dlqPayload.get("failureMessage").asText()).isEqualTo("OpenSearch unavailable");
		assertThat(dlqPayload.get("originalPayload").asText()).isEqualTo(originalPayload);
	}
}
