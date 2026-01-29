package com.example.seonchaksoon.repository;

import com.example.seonchaksoon.domain.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, String> {

    Optional<Event> findByEventKey(String eventKey);

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    Optional<Event> findByEventKey(String eventKey);

//    @Lock(LockModeType.OPTIMISTIC)
//    Optional<Event> findByEventKey(String eventKey);


}
