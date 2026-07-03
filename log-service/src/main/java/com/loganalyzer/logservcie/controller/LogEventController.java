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

@RestController
@RequestMapping("/api/log-events")
@RequiredArgsConstructor
public class LogEventController {

	private final LogEventProducer logEventProducer;

	@PostMapping
	@ResponseStatus(HttpStatus.ACCEPTED)
	public LogEventResponse publish(@Valid @RequestBody LogEventRequest request) {
		return logEventProducer.publish(request);
	}
}
