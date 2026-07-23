package com.loganalyzer.logservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservice.dto.LogEventMessage;
import com.loganalyzer.logservice.dto.LogEventRequest;
import com.loganalyzer.logservice.dto.LogEventResponse;
import com.loganalyzer.logservice.exception.KafkaPublishException;
import com.loganalyzer.logservice.trace.TraceContext;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * HTTP 요청 DTO를 Kafka 로그 이벤트 메시지로 변환하고 지정 토픽으로 발행하는 서비스다.
 *
 * @author butwhole1994
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogEventProducer {

	/** Kafka로 JSON 문자열 메시지를 전송하는 템플릿이다. */
	private final KafkaTemplate<String, String> kafkaTemplate;

	/** 로그 이벤트 메시지를 JSON으로 직렬화하는 매퍼다. */
	private final ObjectMapper objectMapper;

	/** 요청에 서비스명이 없을 때 사용할 애플리케이션 기본 서비스명이다. */
	@Value("${spring.application.name}")
	private String serviceName;

	/** 로그 이벤트를 발행할 Kafka 토픽명이다. */
	@Value("${app.kafka.topics.log-events}")
	private String logEventsTopic;

	/**
	 * 검증된 로그 이벤트 요청을 Kafka 메시지로 변환해 발행한다.
	 *
	 * @param request HTTP API에서 전달된 로그 이벤트 요청
	 * @return 발행 결과와 추적 식별자가 담긴 응답 DTO
	 */
	public LogEventResponse publish(LogEventRequest request) {
		Instant timestamp = request.timestamp();
		String level = normalizeLevel(request.level());
		String sourceServiceName = normalizeText(request.serviceName(), serviceName);
		String loggerName = normalizeText(request.loggerName(), sourceServiceName);
		String threadName = normalizeText(request.threadName(), Thread.currentThread().getName());
		String traceId = TraceContext.resolveTraceId(request.traceId());
		String requestId = TraceContext.resolveRequestId(request.requestId());
		LogEventMessage message = new LogEventMessage(
				UUID.randomUUID().toString(),
				sourceServiceName,
				level,
				loggerName,
				threadName,
				request.message(),
				traceId,
				requestId,
				request.spanId(),
				request.host(),
				request.method(),
				request.path(),
				request.statusCode(),
				request.durationMs(),
				timestamp,
				request.metadata()
		);

		send(message);
		return new LogEventResponse(message.id(), logEventsTopic, traceId, requestId, timestamp);
	}

	/**
	 * 로그 레벨 값을 대문자 표준 형식으로 정규화한다.
	 *
	 * @param level 요청에서 전달된 로그 레벨
	 * @return 비어 있으면 INFO, 값이 있으면 대문자로 변환한 로그 레벨
	 */
	private String normalizeLevel(String level) {
		if (level == null || level.isBlank()) {
			return "INFO";
		}
		return level.trim().toUpperCase(Locale.ROOT);
	}

	/**
	 * 문자열 값을 공백 제거 후 반환하고, 값이 없으면 기본값을 사용한다.
	 *
	 * @param value 요청에서 전달된 문자열 값
	 * @param fallback 값이 비어 있을 때 사용할 기본값
	 * @return 정규화된 문자열 값
	 */
	private String normalizeText(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	/**
	 * 로그 이벤트 메시지를 Kafka 전송용 JSON 문자열로 변환한다.
	 *
	 * @param message 직렬화할 로그 이벤트 메시지
	 * @return JSON 문자열
	 */
	private String toJson(LogEventMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log event", exception);
		}
	}

	/**
	 * Kafka 발행 완료를 제한 시간 안에 확인하고 실패 원인을 서비스 예외로 변환한다.
	 *
	 * @param message 발행할 로그 이벤트 메시지
	 */
	private void send(LogEventMessage message) {
		try {
			kafkaTemplate.send(logEventsTopic, message.id(), toJson(message)).get(5, TimeUnit.SECONDS);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			log.error(
					"Kafka publish interrupted: id={}, topic={}, trace_id={}, request_id={}",
					message.id(),
					logEventsTopic,
					message.traceId(),
					message.requestId(),
					exception
			);
			throw new KafkaPublishException("Failed to publish log event", exception);
		} catch (ExecutionException exception) {
			log.error(
					"Kafka publish failed: id={}, topic={}, trace_id={}, request_id={}",
					message.id(),
					logEventsTopic,
					message.traceId(),
					message.requestId(),
					exception
			);
			throw new KafkaPublishException("Failed to publish log event", exception);
		} catch (TimeoutException exception) {
			log.error(
					"Kafka publish timed out: id={}, topic={}, trace_id={}, request_id={}",
					message.id(),
					logEventsTopic,
					message.traceId(),
					message.requestId(),
					exception
			);
			throw new KafkaPublishException("Failed to publish log event", exception);
		}
	}
}
