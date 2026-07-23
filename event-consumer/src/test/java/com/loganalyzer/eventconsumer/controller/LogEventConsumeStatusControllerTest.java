package com.loganalyzer.eventconsumer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.loganalyzer.eventconsumer.dto.ConsumedLogEventSummary;
import com.loganalyzer.eventconsumer.dto.LogEventConsumeStatusResponse;
import com.loganalyzer.eventconsumer.service.LogEventConsumeStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * LogEventConsumeStatusController가 consume 상태를 표준 API 응답으로 반환하는지 검증한다.
 *
 * @author butwhole1994
 */
class LogEventConsumeStatusControllerTest {

	private final LogEventConsumeStatusService statusService = Mockito.mock(LogEventConsumeStatusService.class);
	private final ObjectMapper objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	private final MockMvc mockMvc = MockMvcBuilders
			.standaloneSetup(new LogEventConsumeStatusController(statusService))
			.setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
			.build();

	/**
	 * 상태 조회 API가 성공 envelope와 마지막 consume 이벤트 정보를 반환하는지 검증한다.
	 *
	 * @throws Exception MockMvc 요청 처리 중 예외가 발생한 경우
	 */
	@Test
	void getStatus_returnsConsumeStatusEnvelope() throws Exception {
		Mockito.when(statusService.getStatus()).thenReturn(new LogEventConsumeStatusResponse(
				"mvp.log-events",
				"event-consumer",
				3,
				Instant.parse("2026-07-03T05:00:00Z"),
				new ConsumedLogEventSummary("event-3", "trace-3", "request-3", "order-service", "INFO")
		));

		mockMvc.perform(get("/api/consumer/log-events/status"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.topic").value("mvp.log-events"))
				.andExpect(jsonPath("$.data.consumerGroupId").value("event-consumer"))
				.andExpect(jsonPath("$.data.consumedCount").value(3))
				.andExpect(jsonPath("$.data.lastConsumedAt").value("2026-07-03T05:00:00Z"))
				.andExpect(jsonPath("$.data.lastEvent.eventId").value("event-3"))
				.andExpect(jsonPath("$.data.lastEvent.traceId").value("trace-3"))
				.andExpect(jsonPath("$.data.lastEvent.requestId").value("request-3"))
				.andExpect(jsonPath("$.data.lastEvent.serviceName").value("order-service"))
				.andExpect(jsonPath("$.data.lastEvent.level").value("INFO"))
				.andExpect(jsonPath("$.meta").doesNotExist())
				.andExpect(jsonPath("$.error").doesNotExist());
	}
}
