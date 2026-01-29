package com.example.seonchaksoon.service.synchronize;

import com.example.seonchaksoon.domain.Coupon;
import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.repository.CouponIssueRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Builder
public class SynchronizedCouponIssueService {

    private static final int LIMIT = 100;
    private final CouponIssueRepository couponIssueRepository;

    public synchronized CouponIssueResponse couponIssue(String eventKey, CouponIssueRequest req) {

        long issuedCount = couponIssueRepository.countByEventKey(eventKey);

        if (issuedCount >= LIMIT) {
            return CouponIssueResponse.builder()
                    .couponId(null)
                    .userId(req.getUserId())
                    .result("SOLD_OUT")
                    .build();
        }

        Coupon saved = couponIssueRepository.saveAndFlush(
                Coupon.builder()
                        .eventKey(eventKey)
                        .userId(req.getUserId())
                        .build()
        );

        return CouponIssueResponse.builder()
                .couponId(saved.getId())
                .userId(saved.getUserId())
                .result("SUCCESS")
                .build();
    }
}
