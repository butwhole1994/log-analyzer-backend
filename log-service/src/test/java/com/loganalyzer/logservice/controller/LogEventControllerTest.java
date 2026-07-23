package com.loganalyzer.logservice.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.loganalyzer.logservice.controller.LogEventController;
import com.loganalyzer.logservice.dto.LogEventResponse;
import com.loganalyzer.logservice.exception.GlobalExceptionHandler;
import com.loganalyzer.logservice.exception.KafkaPublishException;
import com.loganalyzer.logservice.service.LogEventProducer;
import com.loganalyzer.logservice.trace.TraceContext;
import com.loganalyzer.logservice.trace.TraceContextFilter;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.kafka.KafkaException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * LogEventController의 요청 검증, 표준 응답, 추적 헤더 처리를 검증한다.
 *
 * @author butwhole1994
 */
class LogEventControllerTest {

	private MockMvc mockMvc;

	private final LogEventProducer logEventProducer = org.mockito.Mockito.mock(LogEventProducer.class);

	/**
	 * 컨트롤러 단위 테스트에 필요한 MockMvc, 전역 예외 처리기, 검증기, 추적 필터를 구성한다.
	 */
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

	/**
	 * 정상 요청이면 202 응답과 표준 성공 응답 봉투를 반환하는지 검증한다.
	 */
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

	/**
	 * 추적 헤더가 없는 요청에 대해 필터가 trace id와 request id를 생성하는지 검증한다.
	 */
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
				.andExpect(header().exists("X-Trace-Id"))
				.andExpect(header().exists("X-Request-Id"))
				.andExpect(jsonPath("$.data.traceId").exists())
				.andExpect(jsonPath("$.data.requestId").exists());
	}

	/**
	 * 요청 DTO 검증 실패가 표준 오류 응답으로 변환되는지 검증한다.
	 */
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
				.andExpect(header().exists("X-Trace-Id"))
				.andExpect(header().exists("X-Request-Id"))
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

	/**
	 * 잘못된 JSON 본문이 MALFORMED_JSON 오류 코드로 반환되는지 검증한다.
	 */
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

	/**
	 * Kafka 발행 실패가 서비스 일시 불가 오류 응답으로 변환되는지 검증한다.
	 */
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

	/**
	 * 소문자 로그 레벨이 허용되지 않고 검증 오류로 처리되는지 검증한다.
	 */
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
