package com.loganalyzer.logservcie.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TraceContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String traceId = resolveHeader(request, TraceConstants.TRACE_ID_HEADER);
		String requestId = resolveHeader(request, TraceConstants.REQUEST_ID_HEADER);

		MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
		MDC.put(TraceConstants.REQUEST_ID_MDC_KEY, requestId);
		response.setHeader(TraceConstants.TRACE_ID_HEADER, traceId);
		response.setHeader(TraceConstants.REQUEST_ID_HEADER, requestId);

		try {
			filterChain.doFilter(request, response);
		} finally {
			MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
			MDC.remove(TraceConstants.REQUEST_ID_MDC_KEY);
		}
	}

	private String resolveHeader(HttpServletRequest request, String headerName) {
		String value = request.getHeader(headerName);
		if (value == null || value.isBlank()) {
			return TraceContext.generateId();
		}
		return value.trim();
	}
}
