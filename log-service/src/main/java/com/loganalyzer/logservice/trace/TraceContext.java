package com.loganalyzer.logservice.trace;

import java.util.UUID;
import org.slf4j.MDC;

/**
 * 현재 요청의 추적 식별자를 MDC에서 조회하거나 기본값으로 보정하는 유틸리티다.
 *
 * @author butwhole1994
 */
public final class TraceContext {

	private TraceContext() {
	}

	/**
	 * 현재 스레드 MDC에 저장된 trace id를 조회한다.
	 *
	 * @return 현재 trace id, 없으면 null
	 */
	public static String currentTraceId() {
		return MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
	}

	/**
	 * 현재 스레드 MDC에 저장된 request id를 조회한다.
	 *
	 * @return 현재 request id, 없으면 null
	 */
	public static String currentRequestId() {
		return MDC.get(TraceConstants.REQUEST_ID_MDC_KEY);
	}

	/**
	 * 현재 MDC trace id를 우선 사용하고 없으면 요청 본문의 trace id를 사용한다.
	 *
	 * @param fallback 요청 본문에서 전달된 trace id
	 * @return 사용할 trace id
	 */
	public static String resolveTraceId(String fallback) {
		return resolve(currentTraceId(), fallback);
	}

	/**
	 * 현재 MDC request id를 우선 사용하고 없으면 요청 본문의 request id를 사용한다.
	 *
	 * @param fallback 요청 본문에서 전달된 request id
	 * @return 사용할 request id
	 */
	public static String resolveRequestId(String fallback) {
		return resolve(currentRequestId(), fallback);
	}

	/**
	 * 새 추적 식별자로 사용할 UUID 문자열을 생성한다.
	 *
	 * @return UUID 문자열
	 */
	public static String generateId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 현재 컨텍스트 값, fallback 값, 신규 생성값 순서로 사용할 식별자를 결정한다.
	 *
	 * @param current 현재 MDC에 저장된 값
	 * @param fallback 요청 본문에서 전달된 대체 값
	 * @return 최종 식별자 값
	 */
	private static String resolve(String current, String fallback) {
		if (current != null && !current.isBlank()) {
			return current;
		}
		if (fallback != null && !fallback.isBlank()) {
			return fallback.trim();
		}
		return generateId();
	}
}
