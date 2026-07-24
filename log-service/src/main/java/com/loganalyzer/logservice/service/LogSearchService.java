package com.loganalyzer.logservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.loganalyzer.logservice.client.OpenSearchLogSearchClient;
import com.loganalyzer.logservice.dto.LogSearchHit;
import com.loganalyzer.logservice.dto.LogSearchResponse;
import com.loganalyzer.logservice.exception.OpenSearchSearchException;
import com.loganalyzer.logservice.trace.TraceContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Service for basic log search backed by OpenSearch.
 *
 * @author butwhole1994
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogSearchService {

	private final OpenSearchLogSearchClient openSearchLogSearchClient;

	public LogSearchResponse search() {
		String target = openSearchLogSearchClient.readTarget();

		try {
			JsonNode response = openSearchLogSearchClient.search();
			LogSearchResponse searchResponse = toResponse(target, response);

			log.info(
					"OpenSearch log search succeeded: target={}, total_hits={}, returned_hits={}, trace_id={}, request_id={}",
					target,
					searchResponse.totalHits(),
					searchResponse.returnedHits(),
					TraceContext.currentTraceId(),
					TraceContext.currentRequestId()
			);
			return searchResponse;
		} catch (RestClientException exception) {
			log.error(
					"OpenSearch log search failed: target={}, trace_id={}, request_id={}, reason={}",
					target,
					TraceContext.currentTraceId(),
					TraceContext.currentRequestId(),
					exception.getMessage(),
					exception
			);
			throw new OpenSearchSearchException("Failed to search logs from OpenSearch", exception);
		}
	}

	private LogSearchResponse toResponse(String target, JsonNode response) {
		JsonNode hits = response.path("hits").path("hits");
		List<LogSearchHit> searchHits = new ArrayList<>();
		if (hits.isArray()) {
			for (JsonNode hit : hits) {
				searchHits.add(new LogSearchHit(
						textOrNull(hit.path("_id")),
						textOrNull(hit.path("_index")),
						doubleOrNull(hit.path("_score")),
						hit.path("_source")
				));
			}
		}

		JsonNode total = response.path("hits").path("total");
		return new LogSearchResponse(
				target,
				totalHits(total),
				totalHitsRelation(total),
				searchHits.size(),
				integerOrNull(response.path("took")),
				searchHits
		);
	}

	private Long totalHits(JsonNode total) {
		if (total.isNumber()) {
			return total.asLong();
		}
		if (total.path("value").isNumber()) {
			return total.path("value").asLong();
		}
		return 0L;
	}

	private String totalHitsRelation(JsonNode total) {
		if (total.path("relation").isTextual()) {
			return total.path("relation").asText();
		}
		return null;
	}

	private String textOrNull(JsonNode node) {
		if (node.isTextual()) {
			return node.asText();
		}
		return null;
	}

	private Double doubleOrNull(JsonNode node) {
		if (node.isNumber()) {
			return node.asDouble();
		}
		return null;
	}

	private Integer integerOrNull(JsonNode node) {
		if (node.isInt() || node.isLong()) {
			return node.asInt();
		}
		return null;
	}
}
