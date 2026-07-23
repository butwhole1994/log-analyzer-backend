package com.loganalyzer.logservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * log-service 애플리케이션의 Spring Boot 진입점이다.
 *
 * @author butwhole1994
 */
@SpringBootApplication
public class LogServiceApplication {

	/**
	 * 애플리케이션 컨텍스트를 시작한다.
	 *
	 * @param args 실행 시 전달되는 커맨드라인 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(LogServiceApplication.class, args);
	}

}
