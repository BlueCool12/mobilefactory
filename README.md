# 로또 당첨 이벤트 시스템 (Lotto Event System)

본 시스템은 총 10,000명의 참여자를 대상으로 1,000명의 당첨자를 선발하는 이벤트 백엔드입니다. 사전 지정된 1등 당첨자와 무작위로 추출되는 2~4등 당첨자 간의 정합성 문제를 해결하기 위해 **시퀀스 이원화(Sequence Bypass)** 전략을 채택하였습니다.

---

## 1. 핵심 설계 전략

### 시퀀스 이원화 (Sequence Bypass Strategy)
사전 지정된 1등 당첨자가 일반 당첨 슬롯을 소모하여 전체 당첨자 수가 의도보다 줄어드는 결함을 방지하기 위해 카운터를 이원화하여 관리합니다.

* **`totalParticipants` (전체 정원 카운터):** 이벤트의 물리적 마감(10,000명)을 결정합니다. 모든 참여자가 이 카운터를 소모합니다.
* **`poolSequence` (순번 인덱스):** 0~9,998 범위 내에서 일반 참가자의 순번을 결정하는 인덱스입니다. 해당 순번이 `WINNING_SLOT` 테이블에 존재하면 배정된 등수(2~4등)를 부여하고, 테이블에 해당 순번이 없으면(null) 낙첨으로 판정합니다.
* **Bypass Logic:** 사전 지정된 식별자(전화번호)로 참여할 경우 `poolSequence`를 증가시키지 않고 즉시 1등을 부여합니다. 이를 통해 준비된 999개의 일반 당첨 슬롯이 온전히 일반 참여자에게 할당되도록 보장합니다.



### 도메인 중심 설계 (Domain-Driven Design)
서비스 계층의 비대화를 방지하고 비즈니스 규칙의 응집도를 높이기 위해 핵심 판단 로직을 도메인 모델 내부로 캡슐화하였습니다.

* **`LottoEvent`**: 이벤트의 생명주기(진행, 마감, 발표 기간) 검증 및 등수 할당 규칙을 관리합니다.
* **`Participant`**: 결과 확인 횟수(`check_count`) 관리 및 최초 확인 여부에 따른 비즈니스 상태 변경을 담당합니다.



---

## 2. 당첨 및 번호 발급 규칙

### 등수별 구성 및 선정 방식
| 등수 | 당첨 인원 | 선정 방식 | 비고 |
| :--- | :--- | :--- | :--- |
| **1등** | 1명 | 특정 휴대폰 번호 사전 지정 | 확정 보상 (Bypass) |
| **2등** | 5명 | 지정 순번 구간 내 무작위 배치 | `winning_slot` 참조 |
| **3등** | 10명 | 지정 순번 구간 내 무작위 배치 | `winning_slot` 참조 |
| **4등** | 984명 | 전체 순번 구간 내 무작위 배치 | `winning_slot` 참조 |

### 로또 번호 생성 알고리즘 (`LottoNumberGenerator`)
* **정밀한 일치 제어:** 기준 번호와 등수별로 정해진 자릿수만큼만 일치시키고, 나머지 자릿수는 변이(Mutation)를 가합니다.
* **역전 현상 방지:** 미당첨(NONE) 번호 생성 시, 우연히 3자리 이상 일치하여 4등 당첨 조건이 되지 않도록 생성 후 재검증 루틴(`calculateMatchCount`)을 수행합니다.

---

## 3. 기술적 상세 구현

### 동시성 제어 및 장애 복구
* **Atomic 연산:** `AtomicInteger`를 활용하여 멀티스레드 환경에서 순번 채번의 원자성을 보장하며, 검증 실패 시 `decrementAndGet()`을 통해 정합성을 유지합니다.
* **상태 복구:** 서버 재시작 시 `@PostConstruct`를 통해 DB에서 현재 참여 인원과 슬롯 소모 인원을 각각 조회하여 인메모리 카운터를 복구합니다.
* **인증 코드 관리:** `ConcurrentHashMap`을 사용하여 인메모리 내에서 유효시간 내의 인증 번호를 관리합니다.

---

## 4. 데이터베이스 설계 (Database Design)

제공된 DDL을 기반으로 대량의 트래픽 환경에서 당첨 정합성과 운영 편의성을 고려하여 설계된 데이터 구조입니다.

### 4.1 ERD 및 테이블 관계
시스템은 `EVENT`를 중심으로 참여 정보(`PARTICIPANT`), 당첨 자원(`WINNING_SLOT`), 발급 내역(`lotto_number`), 발송 이력(`sms_history`)이 유기적으로 연관된 구조입니다.



### 4.2 테이블 정의 및 주요 컬럼 설계

| 분류 | 테이블명 | 주요 컬럼 및 설계 의도 |
| :--- | :--- | :--- |
| **이벤트 관리** | `EVENT` | `max_participants`: 마감 기준 인원 설정<br>`winning_numbers`: 당첨 판정 기준 번호(6자리)<br>`pre_assigned_phone`: 1등 확정 당첨자 식별 및 Bypass 처리용 |
| **참가자 정보 관리** | `PARTICIPANT` | `phone_number`: Unique 제약으로 중복 참여 원천 차단<br>`is_verified`: SMS 인증 상태 저장 |
| **로또 번호 발번/저장** | `lotto_number` | `numbers`: 생성된 6자리 번호 저장<br>`prize_rank`: 확정된 등수 저장(1~4, 0:낙첨) |
| **당첨 결과 관리** | `WINNING_SLOT` | `sequence_no`: 일반 참여자 시퀀스와 1:1 매핑<br>`prize_rank`: 해당 순번에 배정된 등수(사전 배치) |
| **결과 확인 여부 관리** | `PARTICIPANT` | `check_count`: 결과 조회 시마다 카운팅하여 최초 확인 여부 판별 |
| **문자 발송 이력 관리** | `sms_history` | `category`: 인증번호/당첨안내 등 발송 목적 구분<br>`sent_at`: 발송 시각 기록을 통한 추적성 확보 |

### 4.3 인덱스 전략 (Indexing Strategy)

| 인덱스 명 | 테이블 | 컬럼 | 목적 |
| :--- | :--- | :--- | :--- |
| `idx_slot_event_seq` | `WINNING_SLOT` | `event_id`, `sequence_no` | 시퀀스 번호를 통한 당첨 등수 조회 최적화 |
| `phone_number (Unique)` | `PARTICIPANT` | `phone_number` | 중복 참여 방지 및 참여자 정보 고속 조회 |
| `idx_lotto_participant` | `lotto_number` | `participant_id` | 참여자별 당첨 결과 조회 성능 향상 |
| `idx_sms_participant` | `sms_history` | `participant_id` | 개인별 문자 발송 이력 조회 성능 향상 |

---

### 주요 API 엔드포인트

```http
# 1. 이벤트 상태 조회
# 진행 상태, 현재 참여 인원 정보를 반환합니다.
GET /api/event/status

# 2. 본인 인증번호 발송 (OTP)
# 입력받은 휴대폰 번호로 6자리 인증번호를 생성하여 발송합니다.
POST /api/event/otp
Content-Type: application/json

{
  "phoneNumber": "01012345678"
}

# 3. 이벤트 참여
# 인증번호를 검증하고, 성공 시 등수 판정 및 로또 번호를 발급합니다.
POST /api/event/participate
Content-Type: application/json

{
  "phoneNumber": "01012345678",
  "verificationCode": "123456"
}

# 4. 당첨 결과 확인
# 참여 이력을 확인하고 발급된 로또 번호와 당첨 등수를 반환합니다.
POST /api/event/result
Content-Type: application/json

{
  "phoneNumber": "01012345678"
}
```