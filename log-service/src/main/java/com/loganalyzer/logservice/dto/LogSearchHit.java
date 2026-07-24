package com.loganalyzer.logservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Basic OpenSearch log search hit response.
 *
 * @param id OpenSearch document id
 * @param index OpenSearch index name
 * @param score OpenSearch relevance score
 * @param document indexed log document source
 * @author butwhole1994
 */
public record LogSearchHit(
		String id,
		String index,
		Double score,
		JsonNode document
) {
}
