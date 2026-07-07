# gateway-service

## 역할

`gateway-service`는 외부 요청의 진입점이다.
백엔드 서비스로의 라우팅을 담당하며, 요청 경로와 필터 구성이 핵심이다.

## AI 사용 범위

- Gateway route 설정 초안 작성
- 경로 매핑 리뷰
- 필터 설정 초안 작성
- README 초안 작성
- 라우팅 문제의 원인 후보 분석

## AI 사용 제한

- secret 값 입력 금지
- 운영 credential 입력 금지
- upstream URL은 환경 설정과 일치해야 함
- dependency version은 공식 문서 또는 build 결과로 검증하기

## 사람이 반드시 검토할 항목

- route path가 실제 서비스 경로와 맞는지
- StripPrefix 같은 필터가 의도한 대로 동작하는지
- 외부에 노출되는 진입점이 과도하지 않은지

## AI에게 맡길 수 있는 작업

- route 설정 초안 작성
- 라우팅 규칙 문서화
- 설정 비교 리뷰

## prompt 예시

```text
gateway-service의 Spring Cloud Gateway route 설정을 검토해줘.
조건:
- log-service와 event-consumer 진입점이 명확해야 함
- 경로 prefix 처리 방식이 일관되어야 함
- secret 값은 사용하지 말 것
- 검토 결과에 확인해야 할 항목을 함께 적어줘
```

## 검증 checklist

- [ ] route path가 기대한 서비스로 전달되는가
- [ ] 필터 설정이 경로를 깨지 않는가
- [ ] 실제 실행 경로로 검증했는가

