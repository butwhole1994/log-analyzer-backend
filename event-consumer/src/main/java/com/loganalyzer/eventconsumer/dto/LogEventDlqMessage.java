package com.loganalyzer.eventconsumer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogEventDlqMessage(
		String eventId,
		String traceId,
		String requestId,
		String sourceTopic,
		Integer sourcePartition,
		Long sourceOffset,
		String consumerGroupId,
		String failureType,
		String failureMessage,
		Instant failedAt,
		String originalPayload
) {
}
