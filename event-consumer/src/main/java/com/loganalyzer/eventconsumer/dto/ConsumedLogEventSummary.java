package com.loganalyzer.eventconsumer.dto;

/**
 * 마지막으로 consume에 성공한 로그 이벤트의 핵심 식별 정보다.
 *
 * @param eventId 로그 이벤트 고유 식별자
 * @param traceId 요청 흐름 추적 식별자
 * @param requestId 단일 요청 식별자
 * @param serviceName 로그를 발생시킨 서비스명
 * @param level 로그 레벨
 * @author butwhole1994
 */
public record ConsumedLogEventSummary(
		String eventId,
		String traceId,
		String requestId,
		String serviceName,
		String level
) {
}
