package com.example.seonchaksoon.service.optimisticlock;

import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptimisticCouponIssueFacade {

    private final OptimisticLockCouponIssueService optimisticLockCouponIssueService;

    private static final int MAX_RETRY = 100;

    public CouponIssueResponse issueWithRetry(String eventKey, CouponIssueRequest request) {

        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                return optimisticLockCouponIssueService.issueOnce(eventKey, request);
            } catch (ObjectOptimisticLockingFailureException e) {
                // 충돌 -> 재시도 (잠시 CPU 양보)
                Thread.yield();
            }
        }

        return CouponIssueResponse.builder()
                .couponId(null)
                .userId(request.getUserId())
                .result("LOCK_TIMEOUT")
                .build();
    }
}
