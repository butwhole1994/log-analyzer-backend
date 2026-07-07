package com.loganalyzer.logservcie.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservcie.dto.LogEventMessage;
import com.loganalyzer.logservcie.dto.LogEventRequest;
import com.loganalyzer.logservcie.dto.LogEventResponse;
import java.time.Instant;
import java.util.Locale;
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
		Instant timestamp = request.timestamp() == null ? Instant.now() : request.timestamp();
		String level = normalizeLevel(request.level());
		String loggerName = normalizeText(request.loggerName(), serviceName);
		String threadName = normalizeText(request.threadName(), Thread.currentThread().getName());
		LogEventMessage message = new LogEventMessage(
				UUID.randomUUID().toString(),
				serviceName,
				level,
				loggerName,
				threadName,
				request.message(),
				request.traceId(),
				request.spanId(),
				request.host(),
				request.method(),
				request.path(),
				request.statusCode(),
				request.durationMs(),
				timestamp
		);

		kafkaTemplate.send(logEventsTopic, message.id(), toJson(message));
		return new LogEventResponse(message.id(), logEventsTopic, timestamp);
	}

	private String normalizeLevel(String level) {
		if (level == null || level.isBlank()) {
			return "INFO";
		}
		return level.trim().toUpperCase(Locale.ROOT);
	}

	private String normalizeText(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	private String toJson(LogEventMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log event", exception);
		}
	}
}
