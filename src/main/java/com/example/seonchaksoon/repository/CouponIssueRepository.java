package com.example.seonchaksoon.repository;


import com.example.seonchaksoon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CouponIssueRepository extends JpaRepository<Coupon, Long> {
    long countByEventKey(String eventKey);

//    @Modifying
//    @Transactional
//    void deleteAllByEventKey(String eventKey);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Coupon c where c.eventKey = :eventKey")
    @Transactional
    void deleteAllByEventKey(@Param("eventKey") String eventKey);

}
