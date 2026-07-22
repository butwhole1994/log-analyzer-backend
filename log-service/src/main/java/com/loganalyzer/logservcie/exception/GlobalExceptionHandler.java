package com.loganalyzer.logservcie.exception;

import com.loganalyzer.logservcie.dto.ErrorDetail;
import com.loganalyzer.logservcie.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		List<ErrorDetail> details = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
				.toList();

		log.warn("Log event validation failed: path={}, details={}", request.getRequestURI(), details);
		return build(ErrorCode.VALIDATION_FAILED, request.getRequestURI(), details);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableMessageException(
			HttpMessageNotReadableException exception,
			HttpServletRequest request
	) {
		log.warn("Malformed log event request body: path={}", request.getRequestURI(), exception);
		return build(
				ErrorCode.MALFORMED_JSON,
				request.getRequestURI(),
				List.of(new ErrorDetail("body", "Request body must be valid JSON matching the log event schema"))
		);
	}

	@ExceptionHandler(KafkaPublishException.class)
	public ResponseEntity<ErrorResponse> handleKafkaPublishException(
			KafkaPublishException exception,
			HttpServletRequest request
	) {
		log.error("Kafka publish failed while handling request: path={}", request.getRequestURI(), exception);
		return build(ErrorCode.KAFKA_PUBLISH_FAILED, request.getRequestURI(), List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
		log.error("Unexpected error while handling request: path={}", request.getRequestURI(), exception);
		return build(ErrorCode.INTERNAL_SERVER_ERROR, request.getRequestURI(), List.of());
	}

	private ResponseEntity<ErrorResponse> build(ErrorCode errorCode, String path, List<ErrorDetail> details) {
		return ResponseEntity
				.status(errorCode.status())
				.body(new ErrorResponse(
						Instant.now(),
						path,
						errorCode.name(),
						errorCode.message(),
						details
				));
	}
}
