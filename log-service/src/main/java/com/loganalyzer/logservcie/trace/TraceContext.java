package com.loganalyzer.logservcie.trace;

import java.util.UUID;
import org.slf4j.MDC;

public final class TraceContext {

	private TraceContext() {
	}

	public static String currentTraceId() {
		return MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
	}

	public static String currentRequestId() {
		return MDC.get(TraceConstants.REQUEST_ID_MDC_KEY);
	}

	public static String resolveTraceId(String fallback) {
		return resolve(currentTraceId(), fallback);
	}

	public static String resolveRequestId(String fallback) {
		return resolve(currentRequestId(), fallback);
	}

	public static String generateId() {
		return UUID.randomUUID().toString();
	}

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
