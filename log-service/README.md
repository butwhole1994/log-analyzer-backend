# log-service

## 역할

`log-service`는 로그 입력을 받는 API 계층이다.
입력값을 정규화한 뒤 Kafka 이벤트로 발행하며, Spring Boot + Logback 기본 패턴과 맞물릴 수 있는 로그 메타데이터를 보강한다.

## AI 사용 범위

- Spring Boot 설정 초안 작성
- 로그 입력 DTO 초안 작성
- 로그 발행 API 초안 작성
- Kafka producer 로직 초안 작성
- 테스트 케이스 초안 생성
- README 초안 작성

## AI 사용 제한

- secret 값 입력 금지
- 운영 credential 입력 금지
- Kafka payload 구조를 임의로 바꾸지 않기
- dependency version은 공식 문서 또는 build 결과로 검증하기

## 사람이 반드시 검토할 항목

- 로그 포맷이 서비스 역할에 맞는지
- `level`, `timestamp`, `loggerName`, `threadName` 기본값이 적절한지
- Kafka로 발행되는 JSON 필드가 consumer와 호환되는지
- API 응답 의미가 실제 발행 결과와 일치하는지

## AI에게 맡길 수 있는 작업

- 로그 입력 예시 API 초안 작성
- DTO 필드 정리
- 검증용 테스트 초안 작성
- 로그 포맷 문서 초안 작성

## prompt 예시

```text
log-service에서 사용할 Spring Boot 로그 발행 API 초안을 작성해줘.
조건:
- 요청 DTO와 Kafka 메시지 DTO를 분리할 것
- Spring Boot + Logback 기본 패턴에 맞는 loggerName, threadName, timestamp를 보완할 것
- secret 값은 사용하지 말 것
- 테스트 케이스 초안도 함께 작성할 것
```

## 검증 checklist

- [ ] 입력 DTO와 Kafka 메시지 DTO가 분리되어 있는가
- [ ] 기본값 보정 로직이 명확한가
- [ ] JSON 직렬화 결과가 consumer와 호환되는가
- [ ] 실행 또는 테스트로 검증했는가

