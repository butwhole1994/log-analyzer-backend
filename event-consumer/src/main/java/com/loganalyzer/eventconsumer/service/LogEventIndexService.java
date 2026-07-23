package com.loganalyzer.eventconsumer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.client.OpenSearchLogIndexClient;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import com.loganalyzer.eventconsumer.dto.OpenSearchLogDocument;
import com.loganalyzer.eventconsumer.exception.ConsumerNonRetryableException;
import com.loganalyzer.eventconsumer.exception.ConsumerRetryableException;
import java.time.Clock;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogEventIndexService {

	private final OpenSearchLogIndexClient openSearchLogIndexClient;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Autowired
	public LogEventIndexService(OpenSearchLogIndexClient openSearchLogIndexClient, ObjectMapper objectMapper) {
		this(openSearchLogIndexClient, objectMapper, Clock.systemUTC());
	}

	LogEventIndexService(OpenSearchLogIndexClient openSearchLogIndexClient, ObjectMapper objectMapper, Clock clock) {
		this.openSearchLogIndexClient = openSearchLogIndexClient;
		this.objectMapper = objectMapper;
		this.clock = clock;
	}

	public void index(LogEventMessage message) {
		if (message.eventId() == null || message.eventId().isBlank()) {
			throw new ConsumerNonRetryableException("eventId is required for OpenSearch document ID");
		}
		String targetIndexName = openSearchLogIndexClient.targetIndexName();
		try {
			OpenSearchLogDocument document = OpenSearchLogDocument.from(message, Instant.now(clock));
			openSearchLogIndexClient.save(message.eventId(), objectMapper.writeValueAsString(document));
			log.info(
					"Indexed log event successfully: eventId={}, traceId={}, requestId={}, index={}",
					message.eventId(),
					message.traceId(),
					message.requestId(),
					targetIndexName
			);
		} catch (JsonProcessingException exception) {
			log.error(
					"Failed to serialize log event document: eventId={}, traceId={}, requestId={}, index={}",
					message.eventId(),
					message.traceId(),
					message.requestId(),
					targetIndexName,
					exception
			);
			throw new ConsumerNonRetryableException("Failed to serialize log event document", exception);
		} catch (RuntimeException exception) {
			log.error(
					"Failed to index log event to OpenSearch: eventId={}, traceId={}, requestId={}, index={}",
					message.eventId(),
					message.traceId(),
					message.requestId(),
					targetIndexName,
					exception
			);
			throw new ConsumerRetryableException("Failed to save log event document to OpenSearch", exception);
		}
	}
}
