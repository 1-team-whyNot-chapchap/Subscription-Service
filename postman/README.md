# Subscription Service Postman 검증

## 실행

애플리케이션을 `http://localhost:8082`에서 실행한 뒤 Repository 루트에서 다음 명령을 실행한다.

```powershell
postman collection run postman/chapchap-subscription.postman_collection.json `
  -e postman/environments/local.postman_environment.example.json `
  --bail failure
```

## 자동결제수단 선택 사전조건

- 테스트 고객(`testUserId`, 기본값 `1`)에게 `AVAILABLE` 결제수단이 두 개 이상 존재한다.
- 테스트 고객의 현재 결제수단은 정확히 하나다.
- 다른 고객(`otherUserId`, 기본값 `2`)에게 `AVAILABLE` 결제수단이 하나 이상 존재한다.
- 실행 과정에서 테스트 고객의 다른 결제수단을 현재 수단으로 선택한 뒤 최초 현재 수단으로 복원한다.

Collection은 `GET /api/subscription/payment-methods` 응답에서 필요한 공개 식별자를 자동으로 찾는다. 사전조건이 충족되지 않으면 Setup 단계에서 실패하고 이후 요청을 실행하지 않는다.

## 민감정보

실제 JWT, Secret, PortOne Secret, 빌링키는 Collection 또는 Git에 저장하지 않는다. 개인별 실제 Environment 파일은 `postman/environments/`에 둘 수 있지만 `.gitignore` 대상이며, Git에는 `*.example.postman_environment.json`만 포함한다.

## 검증 한계

Collection은 HTTP 응답과 결제수단 목록 API로 현재 수단 변경·복원을 검증한다. `last_selected_at`이 재선택 시 변경되지 않는지는 고객 API로 노출되지 않으므로 별도의 DB 확인 또는 서비스 테스트가 필요하다.

## SUB-FN-004 첫 구독 Draft Collection

`postman/drafts/sub-fn-004-first-subscription.postman_collection.json`은 첫 구독 API 통합 전에 준비한 초안이다. 아직 위의 기본 CLI 명령에는 포함하지 않으며, 첫 구독 Controller와 통합 흐름이 구현된 뒤 다음과 같이 별도로 실행한다.

```powershell
postman collection run postman/drafts/sub-fn-004-first-subscription.postman_collection.json `
  -e postman/environments/local.postman_environment.json `
  --bail failure
```

실행 전 Git에서 제외된 개인 Local Environment를 만들고 `testPlanId`, `testAddressId`를 실제 로컬 Fixture의 공개 ID로 채운다. 테스트 사용자에게는 현재 필수 약관 동의, 활성 배송지, 현재 `AVAILABLE` 자동결제수단이 필요하다. 시나리오마다 요구하는 구독·약관·결제수단·Provider 상태가 다르므로 Description의 사전조건에 맞게 DB Fixture 또는 제어 가능한 Mock을 재구성한 뒤 해당 요청만 실행한다.

Draft Collection이 확인하는 범위는 HTTP Status, 공통 응답의 `code`·`message`, 공개 구독 ID 형식과 응답 상태다. 다음 항목은 API 응답만으로 확인할 수 없으므로 별도 DB 검증이 필요하다.

- Subscription·Period·Setting·Order의 상태와 생성 개수
- Payment Transaction·Attempt·Allocation의 상태와 생성 여부
- 첫 할인 사용 이력
- 기존 `PROCESSING` 재요청에서 새 데이터와 PG 호출이 생기지 않았는지

Mock 검증은 timeout·5xx 등 결과 분류와 내부 상태 전이를 재현하기 위한 것이며 실제 PortOne 계약·채널 설정·PG별 응답을 검증하지 않는다. 실제 PortOne Test Channel 검증은 별도 승인과 테스트 전용 Credential·빌링키·정리 절차를 준비한 뒤 수행한다.
