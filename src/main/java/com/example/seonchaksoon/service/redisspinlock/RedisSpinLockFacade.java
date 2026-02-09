package com.example.seonchaksoon.service.redisspinlock;

import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.repository.RedisLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class RedisSpinLockFacade {

    private final RedisLockRepository redisLockRepository;
    private final RedisSpinLockService redisSpinLockService;

    public CouponIssueResponse issueWithRedisSpinLock(String eventKey, CouponIssueRequest request) {

        String lockKey = "lock:event:" + eventKey;

        Duration ttl = Duration.ofSeconds(10); // ✅ 3초 -> 10초 (너무 짧으면 만료 위험)
        long maxWaitMs = 2000;                 // ✅ 최대 2초 대기 후 포기
        long start = System.currentTimeMillis();

        String token;
        while ((token = redisLockRepository.tryLock(lockKey, ttl)) == null) {

            if (System.currentTimeMillis() - start > maxWaitMs) {
                return CouponIssueResponse.builder()
                        .couponId(null)
                        .userId(request.getUserId())
                        .result("LOCK_TIMEOUT")  // ✅ 통일
                        .build();
            }

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CouponIssueResponse.builder()
                        .couponId(null)
                        .userId(request.getUserId())
                        .result("INTERRUPTED")
                        .build();
            }
        }

        try {
            return redisSpinLockService.issueOnce(eventKey, request);
        } finally {
            redisLockRepository.unlock(lockKey, token);
        }
    }


}
