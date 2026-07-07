# event-consumer

## 역할

`event-consumer`는 Kafka에서 로그 이벤트를 소비하고 OpenSearch에 저장하는 적재 계층입니다.
Kafka payload를 문서 포맷으로 역직렬화한 뒤, OpenSearch write alias와 ingest pipeline을 사용해 저장합니다.

## 로컬 실행

실행 전 전제:

- PostgreSQL, Kafka, OpenSearch가 로컬에서 접근 가능해야 합니다.
- JDK 17이 필요합니다.

실행:

```bash
./gradlew :event-consumer:bootRun
```

기본 포트:

- `7030`

## 연결 확인

### PostgreSQL

`event-consumer`는 datasource 설정으로 PostgreSQL에 연결합니다.

확인 기준:

- `jdbc:postgresql://localhost:15432/log-analyzer-db`
- 사용자: `admin`
- 비밀번호: `admin1234`

### Kafka consumer

`event-consumer`는 `mvp.log-events` 토픽을 구독합니다.

확인 기준:

- `log-service`에서 이벤트를 발행한 뒤 consumer 로그가 남는지 확인
- payload 역직렬화 실패가 없는지 확인

### OpenSearch

`event-consumer`는 `logs-write` alias와 `logs-pipeline`을 사용합니다.

확인 기준:

- 저장 성공 로그가 남는지 확인
- Dashboards에서 `logs-*` 또는 `logs-read`로 조회 가능한지 확인

## AI 사용 범위

- Kafka consumer 로직 초안 작성
- OpenSearch 저장 클라이언트 초안 작성
- payload 역직렬화 DTO 초안 작성
- 실패 처리와 로그 메시지 초안 작성
- 테스트 케이스 초안 생성

## AI 사용 제한

- secret 값 입력 금지
- 운영 credential 입력 금지
- OpenSearch alias, pipeline, index 이름은 환경 설정과 일치해야 함
- dependency version은 공식 문서 또는 build 결과로 검증하기

## 사람이 반드시 검토할 항목

- Kafka payload와 문서 DTO의 필드가 일치하는지
- OpenSearch에 저장되는 JSON 구조가 검색에 적합한지
- ingest pipeline 적용 경로가 올바른지
- 예외 발생 시 재처리 가능성이 유지되는지

## AI에게 맡길 수 있는 작업

- consumer 흐름 초안 작성
- OpenSearch 저장 요청 코드 초안 작성
- 오류 로그 기반 원인 후보 분석
- 테스트 케이스 초안 생성

## prompt 예시

```text
event-consumer에서 Kafka payload를 받아 OpenSearch에 저장하는 흐름 초안을 작성해줘.
조건:
- Kafka payload DTO와 OpenSearch 문서 DTO를 분리할 것
- OpenSearch write alias와 ingest pipeline을 사용하도록 할 것
- secret 값은 사용하지 말 것
- 실패 시 원인을 추적할 수 있는 로그를 남길 것
```

## 검증 checklist

- [ ] Kafka payload를 정상적으로 역직렬화하는가
- [ ] OpenSearch 저장 JSON이 예상 필드와 일치하는가
- [ ] write alias와 pipeline이 올바르게 사용되는가
- [ ] 테스트 또는 실행으로 검증했는가

