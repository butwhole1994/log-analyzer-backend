package com.loganalyzer.eventconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.client.OpenSearchLogIndexClient;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
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
		LogEventMessage event = deserialize(payload);
		openSearchLogIndexClient.save(event.id(), payload);
		log.info("Stored log event in OpenSearch: id={}, level={}, service={}", event.id(), event.level(), event.serviceName());
	}

	private LogEventMessage deserialize(String payload) {
		try {
			return objectMapper.readValue(payload, LogEventMessage.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Invalid log event payload", exception);
		}
	}
}
