package com.example.seonchaksoon.repository;

import com.example.seonchaksoon.domain.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, String> {

    Optional<Event> findByEventKey(String eventKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update Event e set e.issuedCount = 0 where e.eventKey = :eventKey")
    int resetIssuedCount(@Param("eventKey") String eventKey);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    Optional<Event> findByEventKey(String eventKey);

//    @Lock(LockModeType.OPTIMISTIC)
//    Optional<Event> findByEventKey(String eventKey);


}
