package com.loganalyzer.logservice.dto;

import java.time.Instant;
import java.util.List;

/**
 * HTTP API 실패 응답의 표준 오류 본문이다.
 *
 * @param timestamp 오류 응답을 생성한 시각
 * @param path 오류가 발생한 요청 경로
 * @param code 서비스에서 정의한 오류 코드
 * @param message 사용자에게 전달할 오류 메시지
 * @param details 필드 단위 상세 오류 목록
 * @author butwhole1994
 */
public record ErrorResponse(
		Instant timestamp,
		String path,
		String code,
		String message,
		List<ErrorDetail> details
) {
}
