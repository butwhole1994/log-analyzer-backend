package com.loganalyzer.eventconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.client.OpenSearchLogIndexClient;
import com.loganalyzer.eventconsumer.dto.LogEventDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogEventConsumer {

	private final ObjectMapper objectMapper;
	private final OpenSearchLogIndexClient openSearchLogIndexClient;

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

	private LogEventDocument deserialize(String payload) {
		try {
			return objectMapper.readValue(payload, LogEventDocument.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Invalid log event payload", exception);
		}
	}

	private String toJson(LogEventDocument event) {
		try {
			return objectMapper.writeValueAsString(event);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to serialize log event document", exception);
		}
	}
}
