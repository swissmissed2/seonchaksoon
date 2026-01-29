package com.example.seonchaksoon.service.namedlock;

import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.repository.NamedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;

@Service
@RequiredArgsConstructor
public class NamedLockCouponIssueFacade {

    private final NamedLockRepository namedLockRepository;
    private final NamedLockCouponIssueService namedLockCouponIssueService;

    private static final int TIMEOUT_SEC = 2;

    public CouponIssueResponse issueWithNamedLock(String eventKey, CouponIssueRequest request) {
        String lockName = "event:" + eventKey;

        Connection lockConn = namedLockRepository.tryLock(lockName, TIMEOUT_SEC);
        if (lockConn == null) {
            return CouponIssueResponse.builder()
                    .couponId(null)
                    .userId(request.getUserId())
                    .result("LOCK_TIMEOUT")
                    .build();
        }

        try {
            return namedLockCouponIssueService.issueOnce(eventKey, request);
        } finally {
            namedLockRepository.unlock(lockConn, lockName);
        }
    }
}
