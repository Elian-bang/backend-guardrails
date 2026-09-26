# backend-guardrails

리뷰어 경험에 의존하던 트랜잭션 규칙을 **CI 가 막게** 만들어 보는 실험.

> ⚠️ **정정 (2026-09-14)** — 외부 리뷰를 받고 코드를 다시 검토했다. **지금 상태는 개인 예제에서 동작을 확인한 데모**다.
> 규칙은 직접 만든 샘플 앱과 규칙당 1개의 fixture 로만 검증했고, 대표 규칙 R3 는 현실적인 위반 형태를 놓친다.
> 무엇을 확인했고 무엇을 확인하지 않았는지 아래에 나눠 적었다.

---

## 왜 탐지율만으로는 부족한가

모든 걸 잡는 규칙은 탐지율 100% 지만 쓸모가 없다. 팀이 바로 꺼 버린다. 그래서 검증을 세 층으로 나눈다.

| 층 | 묻는 것 | 없으면 |
|---|---|---|
| ① 탐지 | 위반을 잡는가 | 규칙이 무의미하다 |
| ② **오탐** | **멀쩡한 코드를 안 잡는가** | **팀이 규칙을 끈다** |
| ③ 차단 | CI 에서 실제로 merge 를 막는가 | 문서일 뿐이다 |

그리고 ArchUnit 규칙은 **통과만 하면 죽어 있어도 모른다.** 그래서 규칙을 검사하는 메타 테스트를 따로 둔다.

---

## 지금 있는 것

| 규칙 | 막으려는 것 | 구현 |
|---|---|---|
| R1 · R1b | Controller 에 `@Transactional` (클래스 · 메서드) | ArchUnit |
| R3 | 트랜잭션 안에서 외부 시스템 호출 | ArchUnit — **한계 큼, 아래** |
| R4 | `private` 메서드에 `@Transactional` (프록시가 무시한다) | ArchUnit |
| R6 | Controller 가 Repository 를 직접 호출 | ArchUnit |
| R7 | 트랜잭션 안에서 블로킹 대기 (`Thread.sleep` 등) | ArchUnit |
| R8 | 반복문 안 단건 조회 (N+1) | 런타임 쿼리 수 검사 (`QueryCountTest`) |
| R2 · R5 · R9 · R10 | readOnly · self-invocation · 락 쓰기와 무거운 작업 분리 · 커밋 후 발송 | **미구현** (문서만) |

규칙 10개의 근거와 자동 검증 가능 여부 판정은 [`docs/rules/트랜잭션-규칙-10.md`](docs/rules/트랜잭션-규칙-10.md).

### 확인한 것

- **fixture 매트릭스** (`FixtureMatrixTest`) — 구현한 6개 규칙이 각자의 위반 fixture 를 잡고, 정상 경계 사례에서 오탐 0 (분모는 경계 사례 전체 8개, R6 만 해당 패키지의 3개).
  R3 · R7 은 간접 호출 gap fixture 를 못 잡아 커버리지 50% 로 표시된다
- **메타 테스트** (`RuleSelfTest`) — 첫 실행에서 R1 을 자극하는 위반 fixture 가 없다는 것을 잡았다 (규칙은 클래스 레벨, fixture 는 메서드 레벨이었다)
- **CI 차단** — 같은 PR 에서 위반 커밋은 `BLOCKED`, 제거 커밋은 `CLEAN`. 관리자도 우회할 수 없게 걸었다 → [`docs/CI-파이프라인.md`](docs/CI-파이프라인.md)
  - 이것은 **파이프라인이 동작한다는 확인**이지, 개발 품질이 좋아졌다는 성과가 아니다

### 확인하지 않은 것 — 한계

| 한계 | 내용 |
|---|---|
| **검증 범위** | fixture 는 규칙당 위반 1개 · 정상 경계 사례는 전체 8개, 샘플 앱도 직접 작성했다. **실제 코드에서의 오탐률은 모른다** |
| **R3 대상 목록** | 외부 호출로 보는 타입이 `RestTemplate` · `RestClient` · `WebClient` · `RabbitTemplate` · `KafkaTemplate` · `HttpClient` 6개로 **고정**이다. 샘플 앱이 쓰는 자체 `ChannelClient` 는 목록 밖이다 |
| **R3 호출 깊이** | 트랜잭션 메서드가 **직접** 부르는 호출만 본다. 한 단계만 거쳐도 못 잡는다 |
| **R3 트랜잭션 인식** | 메서드에 직접 붙은 `org.springframework…@Transactional` 만 본다. **클래스 레벨 `@Transactional`**, `jakarta.transaction.Transactional`, `TransactionTemplate` · Spring Batch 트랜잭션은 보지 않는다 |
| **데모 위반의 모양** | CI 차단 데모는 트랜잭션 메서드 안에 `new RestTemplate()` 을 **직접** 넣었다. 샘플 앱의 현실적인 실수 — 트랜잭션 안에서 `NotificationSender` → `ChannelClient` 를 부르는 형태 — 는 코드상 R3 를 **통과한다** (실행으로 확인하지는 않았다) |
| **동기가 된 장애** | 규칙의 동기인 푸시 배치 장애는 Spring Batch 트랜잭션 안에서 여러 단계를 거친 호출이라, 지금 R3 로는 잡히지 않는다 |
| **예외 허용** | 정당한 예외를 허용하는 장치(기준선 · 억제 애노테이션)가 없다. `Guardrails.Level.WARN` 은 선언만 있고 모든 규칙이 차단으로 등록돼 있다 |
| **권고와 판정** | 매트릭스가 오탐이 있으면 `WARN` 을 권고하지만 **빌드 판정에는 반영되지 않는다.** 앱 쪽 규칙 테스트(`GuardrailTest`)를 앱 PR 에서 고치는 우회도 막지 않는다 |
| **R8 쿼리 수 검사** | 한 경로(정산)와 fixture 만 본다. 항목 수가 적으면 N+1 도 허용 기준 안에 들어간다 |
| **sample/msa** | 모듈 3개가 `pom.xml` 만 있고 코드가 없다 (계획) |

---

## v2 에서 할 일 — 대표 규칙 R3 하나를 끝까지

1. **현실적인 fixture** — 자체 클라이언트를 거친 간접 호출, 클래스 레벨 `@Transactional`, `jakarta`, `TransactionTemplate` · Spring Batch, 데모 앱의 원래 실수 형태
2. **규칙 개선** — 외부 호출 대상을 설정할 수 있게(마커 애노테이션 또는 타입 목록), 호출 깊이 N 추적 — 깊이별 탐지 · 오탐 · 분석 시간 측정
3. **예외 처리** — 기준선(`FreezingArchRule`) 또는 사유를 적는 억제 애노테이션, `WARN` 이 실제로 동작하게
4. **실제 코드 검증** — 공개 Spring 프로젝트 2~3개에 돌려 위반마다 진짜/오탐을 직접 판정
5. **문제 사례 → 탐지 → 예외 → CI 실패 메시지** 를 한 번에 읽을 수 있는 예제 문서

---

## 구조

```
backend-guardrails/
├─ guardrails/                         규칙 (TransactionRules · LayerRules · Guardrails)
├─ sample/monolith/                    직접 작성한 좌석 예약 앱 — 규칙을 지키게 짰다
│  └─ src/test/java/dev/elian/
│     ├─ fixtures/violations/          위반 fixture
│     ├─ fixtures/boundary/            위반처럼 보이지만 정상인 경계 사례
│     ├─ fixtures/gaps/                규칙이 못 잡는다고 알려진 사례
│     └─ mono/guard/                   RuleSelfTest · FixtureMatrixTest · GuardrailTest · QueryCountTest
├─ sample/msa/                         (계획 — 코드 없음)
├─ docs/                               규칙 문서 · CI 파이프라인과 차단 증거
└─ .github/workflows/guardrails.yml    규칙 자체 검사 → 앱 검사 / 규칙 변경 감시
```

## R9 · R10 의 출처

두 규칙은 실제 장애의 사후 검증에서 나왔다 — 공지 푸시 배치가 완료 표시를 커밋하지 않은 채 콘텐츠 행을 붙잡아 상세 페이지 조회가 실패한 사건이다.
재현 실험에서 **완료 표시를 즉시 커밋하면 조회 실패가 사라졌다.** 다만 그 실험의 다른 처방 비교는 설계 결함이 있어 정정했다.

> 기록: [커밋을 안 한 트랜잭션이 페이지를 죽인 이야기](https://github.com/Elian-bang/notification-reliability-lab/blob/main/docs/gitbook/%EC%BB%A4%EB%B0%8B%EC%9D%84-%EC%95%88-%ED%95%9C-%ED%8A%B8%EB%9E%9C%EC%9E%AD%EC%85%98%EC%9D%B4-%ED%8E%98%EC%9D%B4%EC%A7%80%EB%A5%BC-%EC%A3%BD%EC%9D%B8-%EC%9D%B4%EC%95%BC%EA%B8%B0.md)
