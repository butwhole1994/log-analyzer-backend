package com.log_analyzer.log_servcie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservcie.dto.LogEventRequest;
import com.loganalyzer.logservcie.dto.LogEventMessage;
import com.loganalyzer.logservcie.service.LogEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class LogEventProducerTest {

	private final KafkaTemplate<String, String> kafkaTemplate = org.mockito.Mockito.mock(KafkaTemplate.class);
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
	private final LogEventProducer producer = new LogEventProducer(kafkaTemplate, objectMapper);

	@Test
	void publish_sendsEventToInfraTopic() {
		ReflectionTestUtils.setField(producer, "serviceName", "log-service");
		ReflectionTestUtils.setField(producer, "logEventsTopic", "mvp.log-events");

		var response = producer.publish(new LogEventRequest("hello", "INFO"));
		var payloadCaptor = org.mockito.ArgumentCaptor.forClass(String.class);

		assertThat(response.topic()).isEqualTo("mvp.log-events");
		assertThat(response.id()).isNotBlank();
		assertThat(response.createdAt()).isNotNull();
		verify(kafkaTemplate).send(eq("mvp.log-events"), eq(response.id()), payloadCaptor.capture());

		LogEventMessage message = org.junit.jupiter.api.Assertions.assertDoesNotThrow(
				() -> objectMapper.readValue(payloadCaptor.getValue(), LogEventMessage.class)
		);
		assertThat(message.id()).isEqualTo(response.id());
		assertThat(message.serviceName()).isEqualTo("log-service");
		assertThat(message.level()).isEqualTo("INFO");
		assertThat(message.message()).isEqualTo("hello");
		assertThat(message.createdAt()).isEqualTo(response.createdAt());
	}
}
