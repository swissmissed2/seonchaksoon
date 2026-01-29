# seonchaksoon – 선착순 쿠폰 발급 시스템

동시 요청 환경에서 **쿠폰 중복 발급을 방지**하고  
**정합성을 보장하는 동시성 제어 방식**을 단계적으로 실험한 프로젝트입니다.

---

## 프로젝트 목적

- 다수의 사용자가 동시에 요청하는 상황에서
  **쿠폰이 초과 발급되지 않도록 제어**
- 단일 서버 → 분산 환경으로 확장 시 발생하는
  **동시성 문제를 직접 구현하고 비교**

---

## 동시성 제어 방식별 구현 흐름

### Java `synchronized`
- 단일 JVM 환경에서는 동작
- ❌ 서버가 여러 대인 경우 무력화

---

### Database Lock
- **Pessimistic Lock**
  - DB Row Lock 기반 제어
  - 안정적이나 트래픽 증가 시 DB 부하 큼
- **Optimistic Lock**
  - 충돌 감지 방식
  - 재시도 로직 필요, 실패 빈도 증가 가능

---

### Redis 기반 Lock

#### Spin Lock (Lettuce)
- Redis `SET NX` 기반 직접 구현
- Lock 획득까지 **반복 요청**
- ❌ Redis 부하 증가 가능

#### Redisson Lock
- Pub/Sub 기반 대기
- 불필요한 반복 요청 제거
- Watchdog을 통한 TTL 자동 연장
- **선착순 쿠폰 시나리오에 가장 적합**

---

## 방식별 비교 요약

| 방식 | 분산 환경 | 성능 | 공정성 | 비고 |
|----|----|----|----|----|
| synchronized | ❌ | ⭐ | ❌ | 단일 서버 |
| DB Lock | ⭕ | ⭐⭐ | ⭕ | DB 부하 |
| Redis Spin Lock | ⭕ | ⭐⭐ | ❌ | Redis 부하 |
| **Redisson Lock** | ⭕ | ⭐⭐⭐⭐ | ⭕ | 최종 선택 |

---

## 테스트

- 멀티스레드 환경에서 동시 요청 테스트
- 쿠폰 발급 수 = 제한 수 정확히 일치
- 중복 발급 발생하지 않음

---

## 핵심 학습 포인트

- 동시성 문제는 **기술 하나로 해결되지 않음**
- 환경(단일/분산), 트래픽, 비용을 고려한 선택이 중요
- Redis Lock도 **구현 방식에 따라 성능 차이가 큼**

---

## Tech Stack

- Java
- Spring Boot
- JPA / Hibernate
- MySQL
- Redis (Lettuce, Redisson)
