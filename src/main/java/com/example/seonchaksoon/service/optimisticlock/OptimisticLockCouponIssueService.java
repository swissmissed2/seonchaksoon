package com.example.seonchaksoon.service.optimisticlock;

import com.example.seonchaksoon.domain.Coupon;
import com.example.seonchaksoon.domain.Event;
import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.dto.CouponIssueResponse;
import com.example.seonchaksoon.repository.CouponIssueRepository;
import com.example.seonchaksoon.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptimisticLockCouponIssueService {

    private final CouponIssueRepository couponIssueRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CouponIssueResponse issueOnce(String eventKey, CouponIssueRequest request) {

        Event event = eventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new IllegalArgumentException("event not found: " + eventKey));

        if (event.isSoldOut()) {
            return CouponIssueResponse.builder()
                    .couponId(null)
                    .userId(request.getUserId())
                    .result("SOLD_OUT")
                    .build();
        }

        event.increaseIssued();

        eventRepository.saveAndFlush(event);

        Coupon saved = couponIssueRepository.save(
                Coupon.builder()
                        .eventKey(eventKey)
                        .userId(request.getUserId())
                        .build()
        );

        return CouponIssueResponse.builder()
                .couponId(saved.getId())
                .userId(saved.getUserId())
                .result("SUCCESS")
                .build();
    }
}
