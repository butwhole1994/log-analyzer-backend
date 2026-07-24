package com.loganalyzer.logservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loganalyzer.logservice.client.OpenSearchLogSearchClient;
import com.loganalyzer.logservice.dto.LogSearchResponse;
import com.loganalyzer.logservice.exception.OpenSearchSearchException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

/**
 * LogSearchService OpenSearch response mapping tests.
 *
 * @author butwhole1994
 */
class LogSearchServiceTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final OpenSearchLogSearchClient openSearchLogSearchClient =
			org.mockito.Mockito.mock(OpenSearchLogSearchClient.class);
	private final LogSearchService logSearchService = new LogSearchService(openSearchLogSearchClient);

	@Test
	void search_mapsOpenSearchHitsToBasicResponse() throws Exception {
		when(openSearchLogSearchClient.readTarget()).thenReturn("logs-read");
		when(openSearchLogSearchClient.search()).thenReturn(objectMapper.readTree("""
				{
				  "took": 5,
				  "hits": {
				    "total": {"value": 2, "relation": "eq"},
				    "hits": [
				      {
				        "_index": "logs-local-000001",
				        "_id": "log-1",
				        "_score": 1.0,
				        "_source": {
				          "serviceName": "order-service",
				          "level": "INFO",
				          "message": "created order"
				        }
				      },
				      {
				        "_index": "logs-local-000001",
				        "_id": "log-2",
				        "_score": 1.0,
				        "_source": {
				          "serviceName": "payment-service",
				          "level": "ERROR",
				          "message": "payment failed"
				        }
				      }
				    ]
				  }
				}
				"""));

		LogSearchResponse response = logSearchService.search();

		assertThat(response.target()).isEqualTo("logs-read");
		assertThat(response.totalHits()).isEqualTo(2L);
		assertThat(response.totalHitsRelation()).isEqualTo("eq");
		assertThat(response.returnedHits()).isEqualTo(2);
		assertThat(response.tookMs()).isEqualTo(5);
		assertThat(response.hits()).hasSize(2);
		assertThat(response.hits().get(0).id()).isEqualTo("log-1");
		assertThat(response.hits().get(0).document().path("serviceName").asText()).isEqualTo("order-service");
	}

	@Test
	void search_wrapsOpenSearchClientFailure() {
		when(openSearchLogSearchClient.readTarget()).thenReturn("logs-read");
		when(openSearchLogSearchClient.search()).thenThrow(new RestClientException("OpenSearch unavailable"));

		assertThatThrownBy(logSearchService::search)
				.isInstanceOf(OpenSearchSearchException.class)
				.hasMessage("Failed to search logs from OpenSearch");
	}
}
