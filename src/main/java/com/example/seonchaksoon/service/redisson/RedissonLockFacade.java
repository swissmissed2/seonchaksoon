package com.example.seonchaksoon.service.redisson;

import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.service.redisspinlock.RedisSpinLockService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedissonLockFacade {

    private final RedissonClient redissonClient;
    private final RedisSpinLockService redisSpinLockService;

    public CouponIssueResponse issueWithRedisson(String eventKey, CouponIssueRequest request) {

        String lockKey = "lock:event:" + eventKey;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(2, TimeUnit.SECONDS);

            if (!locked) {
                return CouponIssueResponse.builder()
                        .userId(request.getUserId())
                        .result("LOCK_TIMEOUT")
                        .build();
            }

            return redisSpinLockService.issueOnce(eventKey, request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CouponIssueResponse.builder()
                    .userId(request.getUserId())
                    .result("INTERRUPTED")
                    .build();
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

