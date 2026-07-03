package com.log_analyzer.event_consumer.service;

import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.eventconsumer.client.OpenSearchLogIndexClient;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import com.loganalyzer.eventconsumer.service.LogEventConsumer;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LogEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final OpenSearchLogIndexClient openSearchLogIndexClient = org.mockito.Mockito.mock(OpenSearchLogIndexClient.class);
	private final LogEventConsumer consumer = new LogEventConsumer(objectMapper, openSearchLogIndexClient);

	@Test
	void consume_savesPayloadToOpenSearchWriteAlias() throws Exception {
		LogEventMessage message = new LogEventMessage(
				"event-1",
				"log-service",
				"INFO",
				"hello",
				Instant.parse("2026-07-03T04:59:03Z")
		);
		String payload = objectMapper.writeValueAsString(message);

		consumer.consume(payload);

		ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(openSearchLogIndexClient).save(idCaptor.capture(), payloadCaptor.capture());
		org.junit.jupiter.api.Assertions.assertEquals("event-1", idCaptor.getValue());
		org.junit.jupiter.api.Assertions.assertEquals(payload, payloadCaptor.getValue());
	}
}
