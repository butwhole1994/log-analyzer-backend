package com.loganalyzer.logservice.client;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OpenSearch log search client.
 *
 * @author butwhole1994
 */
@Component
public class OpenSearchLogSearchClient {

	private static final int DEFAULT_SIZE = 10;

	private final RestClient restClient;
	private final String readTarget;

	public OpenSearchLogSearchClient(
			@Value("${app.opensearch.url}") String openSearchUrl,
			@Value("${app.opensearch.read-target}") String readTarget
	) {
		this.restClient = RestClient.builder()
				.baseUrl(openSearchUrl)
				.build();
		this.readTarget = readTarget;
	}

	public JsonNode search() {
		return restClient.post()
				.uri("/{target}/_search", readTarget)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of(
						"size", DEFAULT_SIZE,
						"query", Map.of("match_all", Map.of())
				))
				.retrieve()
				.body(JsonNode.class);
	}

	public String readTarget() {
		return readTarget;
	}
}
