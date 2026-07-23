# event-consumer

## 역할

`event-consumer`는 `mvp.log-events` Kafka topic에 발행된 로그 이벤트 메시지를 소비하고, JSON payload를 애플리케이션 내부에서 처리 가능한 `LogEventMessage`로 변환한다.

현재 work item의 범위는 Kafka consume과 payload 역직렬화, consume 성공 로그 출력까지다. OpenSearch 색인, retry, DLQ 처리는 후속 작업에서 구현한다.

## Kafka 설정

기본 설정은 로컬 프로필 기준이다.

- topic: `mvp.log-events`
- consumer group: `event-consumer`
- bootstrap servers: `localhost:19092`

환경 변수로 변경할 수 있다.

- `KAFKA_TOPIC_LOG_EVENTS`
- `KAFKA_CONSUMER_GROUP_ID`
- `KAFKA_BOOTSTRAP_SERVERS`

## 실행

JDK 17이 필요하다.

```bash
./gradlew :event-consumer:bootRun
```

Windows PowerShell에서 JDK를 명시해야 하면 다음처럼 실행한다.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\ms-17.0.19'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :event-consumer:bootRun
```

## consume 확인

`log-service`가 `mvp.log-events` topic에 메시지를 발행하면 `event-consumer` 로그에서 다음 필드를 확인한다.

- `eventId`
- `traceId`
- `requestId`
- `serviceName`
- `level`

성공 로그 예시는 다음과 같다.

```text
Consumed log event successfully: eventId=..., traceId=..., requestId=..., serviceName=..., level=...
```

## API 확인

마지막 consume 성공 상태와 누적 consume 성공 건수는 HTTP API로 확인할 수 있다.

```http
GET /api/consumer/log-events/status
```

응답 예시는 다음과 같다.

```json
{
  "success": true,
  "data": {
    "consumedCount": 1,
    "lastConsumedAt": "2026-07-03T04:59:03Z",
    "lastEvent": {
      "eventId": "event-1",
      "traceId": "trace-1",
      "requestId": "request-1",
      "serviceName": "order-service",
      "level": "INFO"
    }
  },
  "meta": null,
  "error": null
}
```

## Kafka UI 확인

Kafka UI에서 다음을 확인한다.

- `Consumers` 또는 `Consumer Groups` 메뉴에서 `event-consumer` group이 표시되는지 확인한다.
- `mvp.log-events` topic에 메시지를 발행한 뒤 group의 offset이 증가하는지 확인한다.
- lag가 계속 증가하면 `event-consumer` 애플리케이션 로그와 Kafka bootstrap server 설정을 먼저 확인한다.

## 제외 범위

이번 구현에서는 다음을 수행하지 않는다.

- OpenSearch indexing
- retry
- DLQ
- Kafka topic 생성 스크립트 수정
- log-service producer 로직 수정
