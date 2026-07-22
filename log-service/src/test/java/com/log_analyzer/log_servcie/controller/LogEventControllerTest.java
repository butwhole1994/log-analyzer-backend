package com.log_analyzer.log_servcie.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.loganalyzer.logservcie.controller.LogEventController;
import com.loganalyzer.logservcie.dto.LogEventResponse;
import com.loganalyzer.logservcie.exception.GlobalExceptionHandler;
import com.loganalyzer.logservcie.exception.KafkaPublishException;
import com.loganalyzer.logservcie.service.LogEventProducer;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.kafka.KafkaException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;

class LogEventControllerTest {

	private MockMvc mockMvc;

	private final LogEventProducer logEventProducer = org.mockito.Mockito.mock(LogEventProducer.class);

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders
				.standaloneSetup(new LogEventController(logEventProducer))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void publish_returnsAcceptedForValidRequest() throws Exception {
		when(logEventProducer.publish(any())).thenReturn(new LogEventResponse(
				"log-1",
				"mvp.log-events",
				Instant.parse("2026-07-03T04:59:03Z")
		));

		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "serviceName": "order-service",
								  "level": "INFO",
								  "message": "created order",
								  "timestamp": "2026-07-03T04:59:03Z",
								  "metadata": {"orderId": "order-1"}
								}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.id").value("log-1"))
				.andExpect(jsonPath("$.topic").value("mvp.log-events"));
	}

	@Test
	void publish_returnsStandardErrorForValidationFailure() throws Exception {
		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "serviceName": "",
								  "level": "FATAL",
								  "message": "",
								  "timestamp": null
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.path").value("/api/logs"))
				.andExpect(jsonPath("$.details[*].field", hasItem("serviceName")))
				.andExpect(jsonPath("$.details[*].field", hasItem("level")))
				.andExpect(jsonPath("$.details[*].field", hasItem("message")))
				.andExpect(jsonPath("$.details[*].field", hasItem("timestamp")));
	}

	@Test
	void publish_returnsStandardErrorForMalformedJson() throws Exception {
		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
				.andExpect(jsonPath("$.path").value("/api/logs"));
	}

	@Test
	void publish_returnsStandardErrorForKafkaPublishFailure() throws Exception {
		when(logEventProducer.publish(any())).thenThrow(new KafkaPublishException(
				"Failed to publish log event",
				new KafkaException("broker unavailable")
		));

		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "serviceName": "order-service",
								  "level": "ERROR",
								  "message": "failed order",
								  "timestamp": "2026-07-03T04:59:03Z"
								}
								"""))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.code").value("KAFKA_PUBLISH_FAILED"))
				.andExpect(jsonPath("$.path").value("/api/logs"));
	}
}
