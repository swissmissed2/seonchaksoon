package com.example.seonchaksoon.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CouponIssueResponse {

    private Long couponId;
    private Long userId;
    private String result; // "SUCCESS" / "SOLD_OUT"
}
