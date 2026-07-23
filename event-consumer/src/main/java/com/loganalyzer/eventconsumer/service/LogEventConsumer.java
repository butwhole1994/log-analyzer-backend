package com.loganalyzer.eventconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * mvp.log-events 토픽의 로그 이벤트 메시지를 소비하고 내부 처리 가능한 메시지로 변환한다.
 *
 * @author butwhole1994
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogEventConsumer {

	/** Kafka payload를 로그 이벤트 메시지 DTO로 역직렬화하는 매퍼다. */
	private final ObjectMapper objectMapper;

	/** consume 성공 여부를 API로 확인할 수 있도록 상태를 기록하는 서비스다. */
	private final LogEventConsumeStatusService consumeStatusService;
	private final LogEventIndexService logEventIndexService;

	/**
	 * Kafka에서 전달된 로그 이벤트 payload를 수신한다.
	 *
	 * @param payload Kafka에서 수신한 JSON 문자열
	 */
	@KafkaListener(
			topics = "${app.kafka.topics.log-events}",
			groupId = "${app.kafka.consumer.group-id}"
	)
	public void consume(String payload) {
		LogEventMessage message = deserialize(payload);
		handle(message);
		log.info(
				"Consumed log event successfully: eventId={}, traceId={}, requestId={}, serviceName={}, level={}",
				message.eventId(),
				message.traceId(),
				message.requestId(),
				message.serviceName(),
				message.level()
		);
	}

	/**
	 * 역직렬화된 로그 이벤트 메시지를 후속 처리 계층으로 전달한다.
	 *
	 * <p>현재 work item에서는 OpenSearch 색인, retry, DLQ 처리를 수행하지 않는다.
	 *
	 * @param message 내부 처리 가능한 로그 이벤트 메시지
	 */
	void handle(LogEventMessage message) {
		logEventIndexService.index(message);
		consumeStatusService.recordConsumed(message);
		log.info(
				"Received log event for processing: eventId={}, traceId={}, requestId={}, serviceName={}, level={}",
				message.eventId(),
				message.traceId(),
				message.requestId(),
				message.serviceName(),
				message.level()
		);
	}

	/**
	 * Kafka payload JSON을 로그 이벤트 메시지 DTO로 역직렬화한다.
	 *
	 * @param payload Kafka에서 수신한 JSON 문자열
	 * @return 역직렬화된 로그 이벤트 메시지
	 */
	LogEventMessage deserialize(String payload) {
		try {
			return objectMapper.readValue(payload, LogEventMessage.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Invalid log event payload", exception);
		}
	}
}
