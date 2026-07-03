package com.loganalyzer.logservcie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservcie.dto.LogEventMessage;
import com.loganalyzer.logservcie.dto.LogEventRequest;
import com.loganalyzer.logservcie.dto.LogEventResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	@Value("${spring.application.name}")
	private String serviceName;

	@Value("${app.kafka.topics.log-events}")
	private String logEventsTopic;

	public LogEventResponse publish(LogEventRequest request) {
		Instant createdAt = Instant.now();
		LogEventMessage message = new LogEventMessage(
				UUID.randomUUID().toString(),
				serviceName,
				request.level() == null || request.level().isBlank() ? "INFO" : request.level(),
				request.message(),
				createdAt
		);

		kafkaTemplate.send(logEventsTopic, message.id(), toJson(message));
		return new LogEventResponse(message.id(), logEventsTopic, createdAt);
	}

	private String toJson(LogEventMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log event", exception);
		}
	}
}
