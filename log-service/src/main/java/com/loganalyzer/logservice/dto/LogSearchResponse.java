package com.loganalyzer.logservice.dto;

import java.util.List;

/**
 * Basic OpenSearch log search API response.
 *
 * @param target searched OpenSearch index or read alias
 * @param totalHits total hit count reported by OpenSearch
 * @param totalHitsRelation relation for the total hit count
 * @param returnedHits number of hits included in this response
 * @param tookMs OpenSearch query duration in milliseconds
 * @param hits returned log search hits
 * @author butwhole1994
 */
public record LogSearchResponse(
		String target,
		Long totalHits,
		String totalHitsRelation,
		Integer returnedHits,
		Integer tookMs,
		List<LogSearchHit> hits
) {
}
