package com.loganalyzer.logservcie.trace;

public final class TraceConstants {

	public static final String TRACE_ID_HEADER = "X-Trace-Id";
	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	public static final String TRACE_ID_MDC_KEY = "trace_id";
	public static final String REQUEST_ID_MDC_KEY = "request_id";

	private TraceConstants() {
	}
}
