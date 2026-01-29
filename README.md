# 선착순 쿠폰 발급 – 동시성 제어

## 문제
선착순 쿠폰 100개 발급 이벤트에서  
동시에 많은 요청이 들어오면 쿠폰 발급 수량 증가 과정에서  
Race Condition이 발생해 **중복·초과 발급** 문제가 생김.

## 목표
- 쿠폰 최대 100개 발급 보장
- 중복 발급 방지
- 단일 서버 → 분산 환경까지 고려한 동시성 제어

## 구현 방식
- **Java synchronized**
  - 단일 서버에서만 유효, 확장성 한계

- **DB Lock**
  - Pessimistic / Optimistic / Named Lock
  - 정확성은 높지만 락 경합 시 성능 저하

- **Redis Spin Lock (Lettuce)**
  - 분산 환경 가능
  - 락 대기 중 Redis에 반복 요청 → 부하 발생

- **Redisson 분산 락**
  - Pub/Sub 기반 대기
  - busy-wait 제거, TTL 자동 관리
  - 운영 환경에 적합하나 락 기반 직렬 처리 한계 존재

## 결론
선착순 쿠폰 발급에서 발생하는 동시성 문제를
애플리케이션, DB, Redis 레벨에서 단계적으로 해결했고,
분산 환경에서는 busy-wait를 제거한 Redisson 분산 락이 가장 안정적인 해결책임을 확인했다.

## 기술 스택
Java, Spring Boot, JPA, MySQL, Redis(Lettuce, Redisson)
