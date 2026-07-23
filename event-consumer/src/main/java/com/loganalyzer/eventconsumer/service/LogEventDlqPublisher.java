package com.loganalyzer.eventconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.dto.LogEventDlqMessage;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogEventDlqPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final Clock clock = Clock.systemUTC();

	@Value("${app.kafka.topics.log-events-dlq}")
	private String dlqTopic;

	@Value("${app.kafka.consumer.group-id}")
	private String consumerGroupId;

	public void publish(ConsumerRecord<?, ?> record, Exception exception) {
		String originalPayload = record.value() == null ? null : String.valueOf(record.value());
		LogEventMessage originalMessage = deserializeOriginalPayload(originalPayload);
		LogEventDlqMessage dlqMessage = new LogEventDlqMessage(
				originalMessage == null ? null : originalMessage.eventId(),
				originalMessage == null ? null : originalMessage.traceId(),
				originalMessage == null ? null : originalMessage.requestId(),
				record.topic(),
				record.partition(),
				record.offset(),
				consumerGroupId,
				exception.getClass().getName(),
				exception.getMessage(),
				Instant.now(clock),
				originalPayload
		);
		String dlqPayload = serializeDlqMessage(dlqMessage);
		String key = record.key() == null ? dlqMessage.eventId() : String.valueOf(record.key());

		log.error(
				"Publishing log event to DLQ: dlqTopic={}, eventId={}, traceId={}, requestId={}, failureType={}, failureMessage={}",
				dlqTopic,
				dlqMessage.eventId(),
				dlqMessage.traceId(),
				dlqMessage.requestId(),
				dlqMessage.failureType(),
				dlqMessage.failureMessage()
		);
		kafkaTemplate.send(dlqTopic, key, dlqPayload);
	}

	private LogEventMessage deserializeOriginalPayload(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(payload, LogEventMessage.class);
		} catch (JsonProcessingException exception) {
			return null;
		}
	}

	private String serializeDlqMessage(LogEventDlqMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize DLQ message", exception);
		}
	}
}
