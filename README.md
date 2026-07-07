# Log Analyzer Backend

`log-analyzer-backend`는 `log-service`, `event-consumer`, `gateway-service`로 구성된 Spring Boot 기반 백엔드입니다.

## 모듈

- `log-service`: 로그 입력을 받아 Kafka 이벤트를 발행합니다.
- `event-consumer`: Kafka 이벤트를 받아 OpenSearch에 저장합니다.
- `gateway-service`: 외부 요청의 진입점을 제공합니다.

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

AI 사용 규칙과 검증 기준은 `docs/ai-rules/`를 따릅니다.

## 로컬 개발 전제

백엔드 실행 전에 PostgreSQL, Kafka, OpenSearch가 로컬에서 접근 가능해야 합니다.
이 저장소의 백엔드는 해당 인프라가 이미 실행되어 있다는 전제에서 동작합니다.

기본 포트:

- PostgreSQL: `15432`
- Kafka: `19092`
- OpenSearch: `19200`
- Gateway: `7010`
- log-service: `7020`
- event-consumer: `7030`

## backend 실행 순서

1. `JAVA_HOME`을 JDK 17로 설정한다.
2. `event-consumer`와 `log-service`가 참조하는 Kafka, PostgreSQL, OpenSearch 접속 정보를 확인한다.
3. `event-consumer`를 먼저 실행한다.
4. `log-service`를 실행한다.
5. `gateway-service`를 실행한다.
6. Gateway를 통해 `log-service` API를 호출해 전체 흐름을 확인한다.

## 연결 확인 기준

### PostgreSQL

`log-service`와 `event-consumer`는 `application-local.yaml`의 datasource 설정으로 PostgreSQL에 연결한다.
연결이 되지 않으면 JPA 초기화가 실패하거나 애플리케이션 시작이 지연된다.

확인 기준:

- `jdbc:postgresql://localhost:15432/log-analyzer-db`
- 사용자: `admin`
- 비밀번호: `admin1234`

### Kafka

`log-service`는 producer로, `event-consumer`는 consumer로 Kafka에 붙는다.

확인 기준:

- 토픽: `mvp.log-events`
- `log-service`: 발행 성공 후 producer 로그가 남아야 함
- `event-consumer`: 수신 후 OpenSearch 저장 로그가 남아야 함

### OpenSearch

`event-consumer`는 OpenSearch write alias와 ingest pipeline을 사용한다.

확인 기준:

- URL: `http://localhost:19200`
- write alias: `logs-write`
- read alias: `logs-read`
- pipeline: `logs-pipeline`

### Gateway route

Gateway는 외부 요청을 `log-service`로 전달한다.

확인 기준:

- Gateway: `http://localhost:7010`
- log-service route: `/log-service/**`
- 실제 호출 경로: `http://localhost:7010/log-service/api/log-events`

## 자주 발생하는 오류

- `Gradle requires JVM 17 or later`: `JAVA_HOME`이 JDK 17이 아님
- `Connection refused` on PostgreSQL: 인프라 DB가 실행되지 않았거나 포트가 다름
- Kafka 토픽 미발견: `mvp.log-events`가 생성되지 않았거나 Kafka 연결 정보가 다름
- OpenSearch 저장 실패: `logs-write` alias 또는 `logs-pipeline` 설정이 맞지 않음
- Gateway 404: `/log-service/**` prefix 또는 StripPrefix 설정이 맞지 않음

## 모듈별 README

- [log-service/README.md](log-service/README.md)
- [event-consumer/README.md](event-consumer/README.md)
- [gateway-service/README.md](gateway-service/README.md)

