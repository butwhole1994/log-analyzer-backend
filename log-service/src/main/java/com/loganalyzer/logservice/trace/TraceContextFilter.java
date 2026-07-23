package com.loganalyzer.logservice.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 모든 HTTP 요청에 trace id와 request id를 부여하고 MDC 및 응답 헤더에 반영하는 필터다.
 *
 * @author butwhole1994
 */
@Component
public class TraceContextFilter extends OncePerRequestFilter {

	/**
	 * 요청 헤더의 추적 식별자를 해석해 MDC에 저장하고 필터 체인 종료 후 정리한다.
	 *
	 * @param request 현재 HTTP 요청
	 * @param response 현재 HTTP 응답
	 * @param filterChain 다음 필터 체인
	 * @throws ServletException 필터 처리 중 서블릿 예외가 발생한 경우
	 * @throws IOException 필터 처리 중 입출력 예외가 발생한 경우
	 */
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

	/**
	 * 지정된 요청 헤더 값을 사용하되, 없거나 비어 있으면 새 식별자를 생성한다.
	 *
	 * @param request 현재 HTTP 요청
	 * @param headerName 조회할 헤더명
	 * @return 헤더 값 또는 새로 생성한 식별자
	 */
	private String resolveHeader(HttpServletRequest request, String headerName) {
		String value = request.getHeader(headerName);
		if (value == null || value.isBlank()) {
			return TraceContext.generateId();
		}
		return value.trim();
	}
}
