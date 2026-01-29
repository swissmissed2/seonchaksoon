package com.example.seonchaksoon.controller;

import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.service.CouponIssueService;
import com.example.seonchaksoon.service.namedlock.NamedLockCouponIssueFacade;
import com.example.seonchaksoon.service.optimisticlock.OptimisticCouponIssueFacade;
import com.example.seonchaksoon.service.pessimisticlock.PessimisticLockCouponIssueService;
import com.example.seonchaksoon.service.redisson.RedissonLockFacade;
import com.example.seonchaksoon.service.redisspinlock.RedisSpinLockFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponIssueController {

    private final CouponIssueService couponIssueService;
    private final PessimisticLockCouponIssueService pessimisticLockCouponIssueService;
    private final OptimisticCouponIssueFacade optimisticCouponIssueFacade;
    private final NamedLockCouponIssueFacade namedLockFacade;
    private final RedisSpinLockFacade redisSpinLockFacade;
    private final RedissonLockFacade redissonLockFacade;

    @PostMapping("/issue/{eventKey}")
    public CouponIssueResponse issue(@PathVariable String eventKey,
                                     @RequestBody CouponIssueRequest request) {
        return redisSpinLockFacade.issueWithRedisSpinLock(eventKey, request);
    }
}
