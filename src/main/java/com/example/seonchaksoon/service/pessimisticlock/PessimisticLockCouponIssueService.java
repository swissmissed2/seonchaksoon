package com.example.seonchaksoon.service.pessimisticlock;

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
public class PessimisticLockCouponIssueService {

    private final EventRepository eventRepository;
    private final CouponIssueRepository couponIssueRepository;

    @Transactional
    public CouponIssueResponse issueCoupon(String eventKey, CouponIssueRequest request) {

        // 1) 이벤트 row를 FOR UPDATE로 잠금
        Event event = eventRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new IllegalArgumentException("event not found: " + eventKey));

        // 2) 여기부터는 같은 eventKey에 대해 한 번에 한 트랜잭션만 통과
        if (event.isSoldOut()) {
            return CouponIssueResponse.builder()
                    .couponId(null)
                    .userId(request.getUserId())
                    .result("SOLD_OUT")
                    .build();
        }

        // 3) 카운트 증가(이벤트 테이블에 반영)
        event.increaseIssued();
        eventRepository.save(event);

        // 4) 성공자 기록 저장
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
