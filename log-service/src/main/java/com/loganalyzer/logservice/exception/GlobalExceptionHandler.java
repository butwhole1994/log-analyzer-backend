package com.loganalyzer.logservice.exception;

import com.loganalyzer.logservice.dto.ApiResponse;
import com.loganalyzer.logservice.dto.ErrorDetail;
import com.loganalyzer.logservice.dto.ErrorResponse;
import com.loganalyzer.logservice.trace.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 컨트롤러에서 발생한 예외를 표준 API 오류 응답으로 변환하는 전역 예외 처리기다.
 *
 * @author butwhole1994
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 요청 DTO 검증 실패를 필드별 상세 오류가 포함된 400 응답으로 변환한다.
	 *
	 * @param exception Bean Validation 검증 실패 예외
	 * @param request 현재 HTTP 요청
	 * @return 표준 검증 실패 응답
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		List<ErrorDetail> details = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
				.toList();

		log.warn(
				"Log event validation failed: path={}, trace_id={}, request_id={}, details={}",
				request.getRequestURI(),
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				details
		);
		return build(ErrorCode.VALIDATION_FAILED, request.getRequestURI(), details);
	}

	/**
	 * 읽을 수 없는 요청 본문을 잘못된 JSON 형식 오류로 변환한다.
	 *
	 * @param exception 요청 본문 역직렬화 실패 예외
	 * @param request 현재 HTTP 요청
	 * @return 표준 JSON 형식 오류 응답
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadableMessageException(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		log.warn(
				"Malformed log event request body: path={}, trace_id={}, request_id={}",
				request.getRequestURI(),
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				exception
		);
		return build(
				ErrorCode.MALFORMED_JSON,
				request.getRequestURI(),
				List.of(new ErrorDetail("body", "Request body must be valid JSON matching the log event schema"))
		);
	}

	/**
	 * Kafka 발행 실패를 서비스 일시 불가 오류로 변환한다.
	 *
	 * @param exception Kafka 발행 중 발생한 서비스 예외
	 * @param request 현재 HTTP 요청
	 * @return 표준 Kafka 발행 실패 응답
	 */
	@ExceptionHandler(KafkaPublishException.class)
	public ResponseEntity<ApiResponse<Void>> handleKafkaPublishException(
			KafkaPublishException exception,
			HttpServletRequest request
	) {
		log.error(
				"Kafka publish failed while handling request: path={}, trace_id={}, request_id={}",
				request.getRequestURI(),
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				exception
		);
		return build(ErrorCode.KAFKA_PUBLISH_FAILED, request.getRequestURI(), List.of());
	}

	/**
	 * 명시적으로 처리하지 않은 예외를 내부 서버 오류 응답으로 변환한다.
	 *
	 * @param exception 처리되지 않은 예외
	 * @param request 현재 HTTP 요청
	 * @return 표준 내부 서버 오류 응답
	 */
	@ExceptionHandler(OpenSearchSearchException.class)
	public ResponseEntity<ApiResponse<Void>> handleOpenSearchSearchException(
			OpenSearchSearchException exception,
			HttpServletRequest request
	) {
		log.error(
				"OpenSearch search failed while handling request: path={}, trace_id={}, request_id={}, reason={}",
				request.getRequestURI(),
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				exception.getMessage(),
				exception
		);
		return build(ErrorCode.OPENSEARCH_SEARCH_FAILED, request.getRequestURI(), List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception, HttpServletRequest request) {
		log.error(
				"Unexpected error while handling request: path={}, trace_id={}, request_id={}",
				request.getRequestURI(),
				TraceContext.currentTraceId(),
				TraceContext.currentRequestId(),
				exception
		);
		return build(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI(), List.of());
	}

	/**
	 * 오류 코드와 상세 정보를 표준 API 응답 봉투로 조립한다.
	 *
	 * @param errorCode 서비스 표준 오류 코드
	 * @param path 오류가 발생한 요청 경로
	 * @param details 필드 단위 상세 오류 목록
	 * @return HTTP 상태가 포함된 표준 실패 응답
	 */
	private ResponseEntity<ApiResponse<Void>> build(ErrorCode errorCode, String path, List<ErrorDetail> details) {
		ErrorResponse error = new ErrorResponse(
				Instant.now(),
				path,
				errorCode.name(),
				errorCode.message(),
				details
		);
		return ResponseEntity
				.status(errorCode.status())
				.body(ApiResponse.fail(error));
	}
}
