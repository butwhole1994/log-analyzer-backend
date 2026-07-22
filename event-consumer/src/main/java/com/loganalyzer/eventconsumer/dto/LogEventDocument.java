package com.loganalyzer.eventconsumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * OpenSearch document shape consumed from the log event Kafka topic.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogEventDocument(
		String id,
		String service,
		String level,
		String loggerName,
		String threadName,
		String message,
		String traceId,
		String requestId,
		String spanId,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs,
		Instant timestamp,
		Map<String, Object> metadata
) {
}
