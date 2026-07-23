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
import com.loganalyzer.logservcie.trace.TraceContext;
import com.loganalyzer.logservcie.trace.TraceContextFilter;
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
				.addFilters(new TraceContextFilter())
				.build();
	}

	@Test
	void publish_returnsAcceptedForValidRequest() throws Exception {
		when(logEventProducer.publish(any())).thenAnswer(invocation -> new LogEventResponse(
				"log-1",
				"mvp.log-events",
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				Instant.parse("2026-07-03T04:59:03Z")
		));

		mockMvc.perform(post("/api/logs")
						.header("X-Trace-Id", "trace-header-1")
						.header("X-Request-Id", "request-header-1")
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
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.id").value("log-1"))
				.andExpect(jsonPath("$.data.topic").value("mvp.log-events"))
				.andExpect(jsonPath("$.data.traceId").value("trace-header-1"))
				.andExpect(jsonPath("$.data.requestId").value("request-header-1"))
				.andExpect(jsonPath("$.meta").doesNotExist())
				.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void publish_setsTraceHeadersWhenMissing() throws Exception {
		when(logEventProducer.publish(any())).thenAnswer(invocation -> new LogEventResponse(
				"log-1",
				"mvp.log-events",
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				Instant.parse("2026-07-03T04:59:03Z")
		));

		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "serviceName": "order-service",
								  "level": "INFO",
								  "message": "created order",
								  "timestamp": "2026-07-03T04:59:03Z"
								}
								"""))
				.andExpect(status().isAccepted())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Trace-Id"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Request-Id"))
				.andExpect(jsonPath("$.data.traceId").exists())
				.andExpect(jsonPath("$.data.requestId").exists());
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
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Trace-Id"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("X-Request-Id"))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.meta").doesNotExist())
				.andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.error.path").value("/api/logs"))
				.andExpect(jsonPath("$.error.details[*].field", hasItem("serviceName")))
				.andExpect(jsonPath("$.error.details[*].field", hasItem("level")))
				.andExpect(jsonPath("$.error.details[*].field", hasItem("message")))
				.andExpect(jsonPath("$.error.details[*].field", hasItem("timestamp")));
	}

	@Test
	void publish_returnsStandardErrorForMalformedJson() throws Exception {
		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("MALFORMED_JSON"))
				.andExpect(jsonPath("$.error.path").value("/api/logs"));
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
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("KAFKA_PUBLISH_FAILED"))
				.andExpect(jsonPath("$.error.path").value("/api/logs"));
	}

	@Test
	void publish_returnsValidationErrorForLowercaseLevel() throws Exception {
		mockMvc.perform(post("/api/logs")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "serviceName": "order-service",
								  "level": "info",
								  "message": "created order",
								  "timestamp": "2026-07-03T04:59:03Z"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
				.andExpect(jsonPath("$.error.details[*].field", hasItem("level")));
	}
}
