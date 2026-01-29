package com.example.seonchaksoon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisLockRepository {

    private final StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    if redis.call('get', KEYS[1]) == ARGV[1] then
                      return redis.call('del', KEYS[1])
                    else
                      return 0
                    end
                    """,
                    Long.class
            );

    /** 락 획득: 성공하면 token, 실패하면 null */
    public String tryLock(String lockKey, Duration ttl) {
        String token = UUID.randomUUID().toString();
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, token, ttl);
        return Boolean.TRUE.equals(ok) ? token : null;
    }

    /** 락 해제: 내 token일 때만 삭제 */
    public boolean unlock(String lockKey, String token) {
        Long result = redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), token);
        return result != null && result == 1L;
    }
}
