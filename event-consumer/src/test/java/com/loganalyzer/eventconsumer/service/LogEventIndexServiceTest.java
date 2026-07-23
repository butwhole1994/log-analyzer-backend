package com.loganalyzer.eventconsumer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.loganalyzer.eventconsumer.client.OpenSearchLogIndexClient;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import com.loganalyzer.eventconsumer.exception.ConsumerNonRetryableException;
import com.loganalyzer.eventconsumer.exception.ConsumerRetryableException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(OutputCaptureExtension.class)
class LogEventIndexServiceTest {

	private static final Instant FIXED_NOW = Instant.parse("2026-07-03T05:00:00Z");

	private final OpenSearchLogIndexClient openSearchLogIndexClient = mock(OpenSearchLogIndexClient.class);
	private final ObjectMapper objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	private final LogEventIndexService indexService = new LogEventIndexService(
			openSearchLogIndexClient,
			objectMapper,
			Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
	);

	@Test
	void index_savesOpenSearchDocumentAndLogsSuccess(CapturedOutput output) throws Exception {
		when(openSearchLogIndexClient.targetIndexName()).thenReturn("logs-write");
		LogEventMessage message = message();

		indexService.index(message);

		ArgumentCaptor<String> documentCaptor = ArgumentCaptor.forClass(String.class);
		verify(openSearchLogIndexClient).save(eq("event-1"), documentCaptor.capture());
		JsonNode document = objectMapper.readTree(documentCaptor.getValue());
		assertThat(document.get("eventId").asText()).isEqualTo("event-1");
		assertThat(document.get("serviceName").asText()).isEqualTo("order-service");
		assertThat(document.get("traceId").asText()).isEqualTo("trace-1");
		assertThat(document.get("requestId").asText()).isEqualTo("request-1");
		assertThat(document.get("level").asText()).isEqualTo("INFO");
		assertThat(document.get("message").asText()).isEqualTo("hello");
		assertThat(document.get("timestamp").asText()).isEqualTo("2026-07-03T04:59:03Z");
		assertThat(document.get("publishedAt").asText()).isEqualTo("2026-07-03T05:00:00Z");
		assertThat(document.get("metadata").get("region").asText()).isEqualTo("ap-northeast-2");
		assertThat(output).contains("Indexed log event successfully");
		assertThat(output).contains("eventId=event-1");
		assertThat(output).contains("traceId=trace-1");
		assertThat(output).contains("requestId=request-1");
		assertThat(output).contains("index=logs-write");
	}

	@Test
	void index_wrapsOpenSearchFailureAsRetryableExceptionAndLogsError(CapturedOutput output) {
		when(openSearchLogIndexClient.targetIndexName()).thenReturn("logs-write");
		LogEventMessage message = message();
		doThrow(new ResourceAccessException("connection refused"))
				.when(openSearchLogIndexClient)
				.save(eq("event-1"), org.mockito.ArgumentMatchers.anyString());

		assertThatThrownBy(() -> indexService.index(message))
				.isInstanceOf(ConsumerRetryableException.class)
				.hasMessage("Failed to save log event document to OpenSearch");
		assertThat(output).contains("Failed to index log event to OpenSearch");
		assertThat(output).contains("eventId=event-1");
		assertThat(output).contains("traceId=trace-1");
		assertThat(output).contains("requestId=request-1");
		assertThat(output).contains("index=logs-write");
	}

	@Test
	void index_rejectsMissingEventIdAsNonRetryableException() {
		LogEventMessage message = new LogEventMessage(
				null,
				"order-service",
				"INFO",
				"com.loganalyzer.LogController",
				"main",
				"hello",
				"trace-1",
				"request-1",
				"span-1",
				"localhost",
				"GET",
				"/health",
				200,
				12L,
				Instant.parse("2026-07-03T04:59:03Z"),
				Map.of("region", "ap-northeast-2")
		);

		assertThatThrownBy(() -> indexService.index(message))
				.isInstanceOf(ConsumerNonRetryableException.class)
				.hasMessage("eventId is required for OpenSearch document ID");
	}

	private LogEventMessage message() {
		return new LogEventMessage(
				"event-1",
				"order-service",
				"INFO",
				"com.loganalyzer.LogController",
				"main",
				"hello",
				"trace-1",
				"request-1",
				"span-1",
				"localhost",
				"GET",
				"/health",
				200,
				12L,
				Instant.parse("2026-07-03T04:59:03Z"),
				Map.of("region", "ap-northeast-2")
		);
	}
}
