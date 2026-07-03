package com.loganalyzer.eventconsumer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenSearchLogIndexClient {

	private final RestClient restClient;
	private final String indexName;

	public OpenSearchLogIndexClient(
			@Value("${app.opensearch.url}") String openSearchUrl,
			@Value("${app.opensearch.index-name}") String indexName
	) {
		this.restClient = RestClient.builder()
				.baseUrl(openSearchUrl)
				.build();
		this.indexName = indexName;
	}

	public void save(String documentId, String documentJson) {
		restClient.post()
				.uri("/{index}/_doc/{id}", indexName, documentId)
				.contentType(MediaType.APPLICATION_JSON)
				.body(documentJson)
				.retrieve()
				.toBodilessEntity();
	}
}
