package com.loganalyzer.logservice.controller;

import com.loganalyzer.logservice.dto.ApiResponse;
import com.loganalyzer.logservice.dto.LogEventRequest;
import com.loganalyzer.logservice.dto.LogEventResponse;
import com.loganalyzer.logservice.service.LogEventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그 이벤트 수집 요청을 받아 Kafka 발행 계층으로 전달하는 HTTP 컨트롤러다.
 *
 * @author butwhole1994
 */
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogEventController {

	private final LogEventProducer logEventProducer;

	/**
	 * 로그 이벤트 요청을 검증한 뒤 발행 결과를 표준 API 응답으로 반환한다.
	 *
	 * @param request 로그 이벤트 발행 요청 본문
	 * @return 발행된 로그 이벤트의 식별자와 추적 정보
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public ApiResponse<LogEventResponse> publish(@Valid @RequestBody LogEventRequest request) {
		return ApiResponse.success(logEventProducer.publish(request));
	}
}
