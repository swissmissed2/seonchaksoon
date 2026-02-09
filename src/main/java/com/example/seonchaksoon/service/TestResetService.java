package com.example.seonchaksoon.service;

import com.example.seonchaksoon.repository.CouponIssueRepository;
import com.example.seonchaksoon.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TestResetService {

    private final EventRepository eventRepository;
    private final CouponIssueRepository couponIssueRepository;

    private final StringRedisTemplate redisTemplate;

    @Transactional
    public ResetResult reset(String eventKey, boolean resetRedis) {
        // 1) DB 쿠폰 발급 기록 삭제
        couponIssueRepository.deleteAllByEventKey(eventKey);

        // 2) 이벤트 issued_count = 0
        int updated = eventRepository.resetIssuedCount(eventKey);

        // 3) (선택) Redis 관련 키 삭제 (테스트 편의용)
        int deletedRedisKeys = 0;
        if (resetRedis) {
            deletedRedisKeys = deleteRedisKeysByEventKey(eventKey);
        }

        return new ResetResult(eventKey, updated, deletedRedisKeys);
    }

    private int deleteRedisKeysByEventKey(String eventKey) {
        Set<String> keys = redisTemplate.keys("*" + eventKey + "*");
        if (keys == null || keys.isEmpty()) return 0;
        Long removed = redisTemplate.delete(keys);
        return removed == null ? 0 : removed.intValue();
    }

    public record ResetResult(String eventKey, int updatedEvents, int deletedRedisKeys) {}
}

