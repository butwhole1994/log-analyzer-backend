package com.loganalyzer.eventconsumer.service;

import com.loganalyzer.eventconsumer.dto.ConsumedLogEventSummary;
import com.loganalyzer.eventconsumer.dto.LogEventConsumeStatusResponse;
import com.loganalyzer.eventconsumer.dto.LogEventMessage;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Kafka 로그 이벤트 consume 성공 상태를 메모리에 보관하는 서비스다.
 *
 * @author butwhole1994
 */
@Service
public class LogEventConsumeStatusService {

	private final AtomicLong consumedCount = new AtomicLong();
	private final AtomicReference<LogEventConsumeStatusResponse> currentStatus;
	private final Clock clock;
	private final String topic;
	private final String consumerGroupId;

	@Autowired
	public LogEventConsumeStatusService(
			@Value("${app.kafka.topics.log-events}") String topic,
			@Value("${app.kafka.consumer.group-id}") String consumerGroupId
	) {
		this(Clock.systemUTC(), topic, consumerGroupId);
	}

	LogEventConsumeStatusService(Clock clock) {
		this(clock, "mvp.log-events", "event-consumer");
	}

	LogEventConsumeStatusService(Clock clock, String topic, String consumerGroupId) {
		this.clock = clock;
		this.topic = topic;
		this.consumerGroupId = consumerGroupId;
		this.currentStatus = new AtomicReference<>(
				new LogEventConsumeStatusResponse(topic, consumerGroupId, 0, null, null)
		);
	}

	/**
	 * consume에 성공한 로그 이벤트의 핵심 정보를 상태로 기록한다.
	 *
	 * @param message consume에 성공한 로그 이벤트 메시지
	 */
	public void recordConsumed(LogEventMessage message) {
		long nextCount = consumedCount.incrementAndGet();
		ConsumedLogEventSummary summary = new ConsumedLogEventSummary(
				message.eventId(),
				message.traceId(),
				message.requestId(),
				message.serviceName(),
				message.level()
		);
		currentStatus.set(new LogEventConsumeStatusResponse(
				topic,
				consumerGroupId,
				nextCount,
				Instant.now(clock),
				summary
		));
	}

	/**
	 * 현재까지의 consume 성공 상태를 조회한다.
	 *
	 * @return consume 상태 응답 데이터
	 */
	public LogEventConsumeStatusResponse getStatus() {
		return currentStatus.get();
	}
}
