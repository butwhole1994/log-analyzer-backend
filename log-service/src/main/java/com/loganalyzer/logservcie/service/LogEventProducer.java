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

/**
 * 로그 요청을 Kafka 이벤트로 변환해 발행하는 서비스다.
 *
 * <p>역할:
 * - API 계층에서 들어온 로그 입력을 정규화한다.
 * - Spring Boot + Logback 기본 패턴과 호환 가능한 메타데이터를 보완한다.
 * - Kafka로 전달할 직렬화된 메시지를 만든다.
 *
 * @author butwhole1994
 */
@Service
@RequiredArgsConstructor
public class LogEventProducer {

	// Kafka로 JSON 문자열을 전송하기 위한 템플릿이다.
	private final KafkaTemplate<String, String> kafkaTemplate;

	// 요청과 이벤트 객체를 JSON으로 직렬화하기 위한 ObjectMapper다.
	private final ObjectMapper objectMapper;

	// 현재 애플리케이션의 서비스명이다.
	@Value("${spring.application.name}")
	private String serviceName;

	// 로그 이벤트를 발행할 Kafka 토픽명이다.
	@Value("${app.kafka.topics.log-events}")
	private String logEventsTopic;

	/**
	 * 로그 요청을 정규화한 뒤 Kafka 이벤트로 발행한다.
	 *
	 * @param request API에서 전달된 로그 입력값
	 * @return 발행 결과를 담은 응답 DTO
	 */
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

	/**
	 * 로그 레벨을 표준 대문자 형식으로 정규화한다.
	 *
	 * @param level 입력된 로그 레벨
	 * @return 기본값이 적용된 로그 레벨
	 */
	private String normalizeLevel(String level) {
		if (level == null || level.isBlank()) {
			return "INFO";
		}
		return level.trim().toUpperCase(Locale.ROOT);
	}

	/**
	 * 문자열 값을 기본값 기준으로 보정한다.
	 *
	 * @param value 입력값
	 * @param fallback 비어 있을 때 사용할 기본값
	 * @return 정규화된 문자열
	 */
	private String normalizeText(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	/**
	 * 이벤트 객체를 Kafka 전송용 JSON 문자열로 변환한다.
	 *
	 * @param message 직렬화할 이벤트 객체
	 * @return JSON 문자열
	 */
	private String toJson(LogEventMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log event", exception);
		}
	}
}
