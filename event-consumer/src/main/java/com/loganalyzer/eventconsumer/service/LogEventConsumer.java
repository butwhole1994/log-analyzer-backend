package com.loganalyzer.eventconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.client.OpenSearchLogIndexClient;
import com.loganalyzer.eventconsumer.dto.LogEventDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka로 들어온 로그 이벤트를 OpenSearch에 적재하는 소비자 서비스다.
 *
 * <p>역할:
 * - Kafka payload를 문서 DTO로 역직렬화한다.
 * - OpenSearch 저장용 JSON으로 다시 직렬화한다.
 * - 적재 결과를 애플리케이션 로그로 남긴다.
 *
 * @author butwhole1994
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogEventConsumer {

	// Kafka payload를 문서 DTO로 바꾸기 위한 ObjectMapper다.
	private final ObjectMapper objectMapper;

	// OpenSearch에 문서를 저장하는 클라이언트다.
	private final OpenSearchLogIndexClient openSearchLogIndexClient;

	/**
	 * Kafka payload를 문서로 변환한 뒤 OpenSearch에 저장한다.
	 *
	 * @param payload Kafka에서 전달된 JSON 문자열
	 */
	@KafkaListener(topics = "${app.kafka.topics.log-events}")
	public void consume(String payload) {
		LogEventDocument event = deserialize(payload);
		openSearchLogIndexClient.save(event.id(), toJson(event));
		log.info(
				"Stored log event in OpenSearch: id={}, level={}, service={}, logger={}",
				event.id(),
				event.level(),
				event.service(),
				event.loggerName()
		);
	}

	/**
	 * Kafka payload를 문서 DTO로 역직렬화한다.
	 *
	 * @param payload Kafka에서 받은 JSON 문자열
	 * @return 문서 DTO
	 */
	private LogEventDocument deserialize(String payload) {
		try {
			return objectMapper.readValue(payload, LogEventDocument.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Invalid log event payload", exception);
		}
	}

	/**
	 * 문서 DTO를 OpenSearch 저장용 JSON 문자열로 변환한다.
	 *
	 * @param event 저장할 문서 DTO
	 * @return JSON 문자열
	 */
	private String toJson(LogEventDocument event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log event document", exception);
		}
	}
}
