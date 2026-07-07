# Log Analyzer Backend

`log-analyzer-backend`는 `log-service`, `event-consumer`, `gateway-service`로 구성된 Spring Boot 기반 백엔드입니다.

## 구조

- `log-service`: 로그 입력을 받아 Kafka 이벤트를 발행합니다.
- `event-consumer`: Kafka 이벤트를 받아 OpenSearch에 저장합니다.
- `gateway-service`: 외부 요청의 진입점을 제공합니다.
- `docs/ai-rules`: Agentic AI 사용 규칙과 검증 기준을 담습니다.

## AI-assisted backend development workflow

이 저장소는 Agentic AI를 단순 코드 생성 도구가 아니라 개발 보조 워크플로우로 사용합니다.

AI는 다음 작업에 활용합니다.

- Spring Boot 설정 초안 생성
- Gateway route 설정 리뷰
- Docker Compose 설정 초안 리뷰
- Kafka / Redis 연결 코드 초안 작성
- 오류 로그 기반 원인 후보 분석
- README 초안 작성
- 테스트 케이스 초안 생성

AI 사용 시 반드시 지키는 기준은 `docs/ai-rules/`를 따른다.

## 모듈별 README

- [log-service/README.md](log-service/README.md)
- [event-consumer/README.md](event-consumer/README.md)
- [gateway-service/README.md](gateway-service/README.md)

