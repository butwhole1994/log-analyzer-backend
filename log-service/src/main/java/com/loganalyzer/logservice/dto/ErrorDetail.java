package com.loganalyzer.logservice.dto;

/**
 * 검증 실패 등에서 특정 필드별 오류 원인을 표현한다.
 *
 * @param field 오류가 발생한 필드명
 * @param reason 해당 필드가 실패한 이유
 * @author butwhole1994
 */
public record ErrorDetail(
		String field,
		String reason
) {
}
