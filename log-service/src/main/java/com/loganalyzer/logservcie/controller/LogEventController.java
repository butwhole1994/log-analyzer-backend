package com.loganalyzer.logservcie.controller;

import com.loganalyzer.logservcie.dto.LogEventRequest;
import com.loganalyzer.logservcie.dto.LogEventResponse;
import com.loganalyzer.logservcie.service.LogEventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그 저장 테스트 API를 제공하는 컨트롤러다.
 *
 * <p>이 API는 log-service가 로그 포맷을 정의하고 Kafka로 넘기는 역할을 검증하기 위한 진입점이다.
 *
 * @author butwhole1994
 */
@RestController
@RequestMapping("/api/log-events")
@RequiredArgsConstructor
public class LogEventController {

	// 로그 이벤트 발행을 담당하는 서비스다.
	private final LogEventProducer logEventProducer;

	/**
	 * 로그 저장 요청을 받아 Kafka 발행 결과를 반환한다.
	 *
	 * @param request 로그 입력 DTO
	 * @return 발행 결과 DTO
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public LogEventResponse publish(@Valid @RequestBody LogEventRequest request) {
		return logEventProducer.publish(request);
	}
}
