package com.example.seonchaksoon.dto;

import lombok.Builder;

@Builder
public class CouponIssueResponse {

    private Long couponId;
    private Long userId;
    private String result; // "SUCCESS" / "SOLD_OUT"
}
