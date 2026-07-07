package com.loganalyzer.eventconsumer.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OpenSearch 문서 저장을 담당하는 클라이언트다.
 *
 * <p>Kafka 처리 결과를 write alias와 ingest pipeline을 통해 적재한다.
 *
 * @author butwhole1994
 */
@Component
public class OpenSearchLogIndexClient {

	// OpenSearch 호출용 RestClient다.
	private final RestClient restClient;

	// 문서를 저장할 write alias 또는 인덱스명이다.
	private final String indexName;

	// 저장 시 함께 적용할 ingest pipeline 이름이다.
	private final String pipelineName;

	public OpenSearchLogIndexClient(
			@Value("${app.opensearch.url}") String openSearchUrl,
			@Value("${app.opensearch.index-name}") String indexName,
			@Value("${app.opensearch.pipeline-name}") String pipelineName
	) {
		this.restClient = RestClient.builder()
				.baseUrl(openSearchUrl)
				.build();
		this.indexName = indexName;
		this.pipelineName = pipelineName;
	}

	/**
	 * OpenSearch write alias에 문서를 저장한다.
	 *
	 * <p>저장 시 ingest pipeline을 함께 지정해 적재 전처리를 적용한다.
	 *
	 * @param documentId 문서 식별자
	 * @param documentJson 저장할 JSON 문자열
	 */
	public void save(String documentId, String documentJson) {
		restClient.post()
				.uri(uriBuilder -> uriBuilder
						.path("/{index}/_doc/{id}")
						.queryParam("pipeline", pipelineName)
						.build(indexName, documentId))
				.contentType(MediaType.APPLICATION_JSON)
				.body(documentJson)
				.retrieve()
				.toBodilessEntity();
	}
}
