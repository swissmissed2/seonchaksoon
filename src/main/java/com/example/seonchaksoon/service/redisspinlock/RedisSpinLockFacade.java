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
        Duration ttl = Duration.ofSeconds(10);

        String token;
        while ((token = redisLockRepository.tryLock(lockKey, ttl)) == null) {
            try {
                long backoff = 10 + (long) (Math.random() * 41); // 10~50ms
                Thread.sleep(backoff);
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
