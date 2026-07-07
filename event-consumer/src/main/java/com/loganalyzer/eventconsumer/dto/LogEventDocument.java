package com.loganalyzer.eventconsumer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

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
		String spanId,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs,
		Instant timestamp
) {
}
