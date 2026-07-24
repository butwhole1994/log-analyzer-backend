package com.loganalyzer.logservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservice.dto.LogSearchHit;
import com.loganalyzer.logservice.dto.LogSearchResponse;
import com.loganalyzer.logservice.exception.GlobalExceptionHandler;
import com.loganalyzer.logservice.exception.OpenSearchSearchException;
import com.loganalyzer.logservice.service.LogSearchService;
import com.loganalyzer.logservice.trace.TraceContextFilter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;

/**
 * LogSearchController response envelope tests.
 *
 * @author butwhole1994
 */
class LogSearchControllerTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final LogSearchService logSearchService = org.mockito.Mockito.mock(LogSearchService.class);

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new LogSearchController(logSearchService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.addFilters(new TraceContextFilter())
				.build();
	}

	@Test
	void search_returnsStandardSuccessResponse() throws Exception {
		when(logSearchService.search()).thenReturn(new LogSearchResponse(
				"logs-read",
				1L,
				"eq",
				1,
				7,
				List.of(new LogSearchHit(
						"log-1",
						"logs-local-000001",
						1.0,
						objectMapper.readTree("""
								{
								  "serviceName": "order-service",
								  "level": "INFO",
								  "message": "created order"
								}
								""")
				))
		));

		mockMvc.perform(get("/api/logs/search")
						.header("X-Trace-Id", "trace-header-1")
						.header("X-Request-Id", "request-header-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.target").value("logs-read"))
				.andExpect(jsonPath("$.data.totalHits").value(1))
				.andExpect(jsonPath("$.data.returnedHits").value(1))
				.andExpect(jsonPath("$.data.hits[0].id").value("log-1"))
				.andExpect(jsonPath("$.data.hits[0].document.serviceName").value("order-service"))
				.andExpect(jsonPath("$.meta").doesNotExist())
				.andExpect(jsonPath("$.error").doesNotExist());
	}

	@Test
	void search_returnsStandardErrorForOpenSearchFailure() throws Exception {
		when(logSearchService.search()).thenThrow(new OpenSearchSearchException(
				"Failed to search logs from OpenSearch",
				new RestClientException("OpenSearch unavailable")
		));

		mockMvc.perform(get("/api/logs/search"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(header().exists("X-Trace-Id"))
				.andExpect(header().exists("X-Request-Id"))
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.meta").doesNotExist())
				.andExpect(jsonPath("$.error.code").value("OPENSEARCH_SEARCH_FAILED"))
				.andExpect(jsonPath("$.error.path").value("/api/logs/search"));
	}
}
