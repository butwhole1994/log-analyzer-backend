package com.loganalyzer.logservice.dto;

/**
 * 목록형 API 응답에 사용할 페이지네이션 및 정렬 메타데이터다.
 *
 * @param page 현재 페이지 번호
 * @param size 페이지 크기
 * @param totalElements 전체 요소 수
 * @param totalPages 전체 페이지 수
 * @param sort 적용된 정렬 조건
 * @author butwhole1994
 */
public record ResponseMeta(
		Integer page,
		Integer size,
		Long totalElements,
		Integer totalPages,
		String sort
) {
}
