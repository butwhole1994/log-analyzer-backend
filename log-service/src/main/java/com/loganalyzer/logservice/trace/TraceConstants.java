package com.loganalyzer.logservice.trace;

/**
 * 요청 추적에 사용하는 HTTP 헤더명과 MDC 키를 모아 둔 상수 클래스다.
 *
 * @author butwhole1994
 */
public final class TraceConstants {

	/** 외부 요청 흐름을 식별하는 trace id HTTP 헤더명이다. */
	public static final String TRACE_ID_HEADER = "X-Trace-Id";
	/** 단일 HTTP 요청을 식별하는 request id HTTP 헤더명이다. */
	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	/** 로깅 MDC에 저장하는 trace id 키다. */
	public static final String TRACE_ID_MDC_KEY = "trace_id";
	/** 로깅 MDC에 저장하는 request id 키다. */
	public static final String REQUEST_ID_MDC_KEY = "request_id";

	private TraceConstants() {
	}
}
