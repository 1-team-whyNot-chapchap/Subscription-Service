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
