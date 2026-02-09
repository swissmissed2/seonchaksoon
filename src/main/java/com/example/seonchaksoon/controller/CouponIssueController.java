package com.example.seonchaksoon.controller;

import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.service.CouponIssueService;
import com.example.seonchaksoon.service.TestResetService;
import com.example.seonchaksoon.service.namedlock.NamedLockCouponIssueFacade;
import com.example.seonchaksoon.service.optimisticlock.OptimisticCouponIssueFacade;
import com.example.seonchaksoon.service.pessimisticlock.PessimisticLockCouponIssueService;
import com.example.seonchaksoon.service.redisson.RedissonLockFacade;
import com.example.seonchaksoon.service.redisspinlock.RedisSpinLockFacade;
import com.example.seonchaksoon.service.synchronize.SynchronizedCouponIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponIssueController {

    private final CouponIssueService couponIssueService;
    private final SynchronizedCouponIssueService synchronizedCouponIssueService;
    private final PessimisticLockCouponIssueService pessimisticLockCouponIssueService;
    private final OptimisticCouponIssueFacade optimisticCouponIssueFacade;
    private final NamedLockCouponIssueFacade namedLockFacade;
    private final RedisSpinLockFacade redisSpinLockFacade;
    private final RedissonLockFacade redissonLockFacade;
    private final TestResetService testResetService;

    @PostMapping("/issue/{strategy}/{eventKey}")
    public ResponseEntity<CouponIssueResponse> issue(
            @PathVariable String strategy,
            @PathVariable String eventKey,
            @RequestBody CouponIssueRequest request
    ) {
        CouponIssueResponse res = switch (strategy) {
            case "synchronized" -> synchronizedCouponIssueService.couponIssue(eventKey, request);
            case "pessimistic" -> pessimisticLockCouponIssueService.issueCoupon(eventKey, request);
            case "optimistic" -> optimisticCouponIssueFacade.issueWithRetry(eventKey, request);
            case "named" -> namedLockFacade.issueWithNamedLock(eventKey, request);
            case "redis-spin" -> redisSpinLockFacade.issueWithRedisSpinLock(eventKey, request);
            case "redisson" -> redissonLockFacade.issueWithRedisson(eventKey, request);
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        };

        return toResponse(res);
    }

    private ResponseEntity<CouponIssueResponse> toResponse(CouponIssueResponse res) {
        String result = res.getResult();

        if ("SUCCESS".equals(result)) {
            return ResponseEntity.ok(res);
        }
        if ("SOLD_OUT".equals(result)) {
            return ResponseEntity.status(409).body(res);
        }
        if ("LOCK_TIMEOUT".equals(result) || "INTERRUPTED".equals(result)) {
            return ResponseEntity.status(503).body(res);
        }
        return ResponseEntity.status(500).body(res);
    }

    @PostMapping("/reset/{eventKey}")
    public TestResetService.ResetResult reset(
            @PathVariable String eventKey,
            @RequestParam(defaultValue = "false") boolean resetRedis
    ) {
        return testResetService.reset(eventKey, resetRedis);
    }
}
