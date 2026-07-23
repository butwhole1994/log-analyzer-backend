package com.loganalyzer.eventconsumer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenSearchLogDocument(
		String eventId,
		String serviceName,
		String level,
		String message,
		String traceId,
		String requestId,
		Instant timestamp,
		Instant publishedAt,
		Map<String, Object> metadata,
		String loggerName,
		String threadName,
		String spanId,
		String host,
		String method,
		String path,
		Integer statusCode,
		Long durationMs
) {

	public static OpenSearchLogDocument from(LogEventMessage message, Instant publishedAt) {
		return new OpenSearchLogDocument(
				message.eventId(),
				message.serviceName(),
				message.level(),
				message.message(),
				message.traceId(),
				message.requestId(),
				message.timestamp(),
				publishedAt,
				message.metadata(),
				message.loggerName(),
				message.threadName(),
				message.spanId(),
				message.host(),
				message.method(),
				message.path(),
				message.statusCode(),
				message.durationMs()
		);
	}
}
