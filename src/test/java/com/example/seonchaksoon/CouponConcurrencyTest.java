package com.example.seonchaksoon;

import com.example.seonchaksoon.domain.Event;
import com.example.seonchaksoon.dto.CouponIssueRequest;
import com.example.seonchaksoon.repository.CouponIssueRepository;
import com.example.seonchaksoon.repository.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CouponConcurrencyTest {

    @LocalServerPort
    int port;

    @Autowired
    CouponIssueRepository couponIssueRepository;
    @Autowired
    EventRepository eventRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ExecutorService executor = Executors.newFixedThreadPool(50);  // namedlpck 테스트 시 10으로 조정

    @BeforeEach
    void setUp() {
        // 쿠폰 초기화
        couponIssueRepository.deleteAllByEventKey("EVENT_1");

        // 이벤트 초기화
        eventRepository.deleteById("EVENT_1");

        Event event = new Event("EVENT_1", 100); // 생성자 사용
        eventRepository.save(event);
    }

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void 동시요청_테스트() throws Exception {
        int total = 1000;
        String eventKey = "EVENT_1";
        String url = "http://localhost:" + port + "/coupons/issue/" + eventKey;

        CountDownLatch startGate = new CountDownLatch(1);   // 동시에 출발
        CountDownLatch endGate = new CountDownLatch(total); // 전부 끝날 때까지 대기

        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();
        AtomicInteger errors  = new AtomicInteger();

        for (int i = 1; i <= total; i++) {
            long userId = i;

            executor.submit(() -> {
                try {
                    startGate.await(); // 출발 신호 대기

                    CouponIssueRequest req = new CouponIssueRequest();
                    req.setUserId(userId);

                    HttpEntity<CouponIssueRequest> entity = new HttpEntity<>(req);

                    ResponseEntity<String> response =
                            restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                    String body = response.getBody() == null ? "" : response.getBody();

                    if (body.contains("SUCCESS")) success.incrementAndGet();
                    else if (body.contains("SOLD_OUT")) soldOut.incrementAndGet();
                    else errors.incrementAndGet();

                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // 여기 한 방으로 "동시에" 시작
        startGate.countDown();

        // 너무 오래 걸리면 테스트가 무한정 기다리지 않게 타임아웃
        boolean finished = endGate.await(30, TimeUnit.SECONDS);
        System.out.println("finished = " + finished);

        long savedCount = couponIssueRepository.countByEventKey(eventKey);

        System.out.println("SUCCESS = " + success.get());
        System.out.println("SOLD_OUT = " + soldOut.get());
        System.out.println("ERRORS  = " + errors.get());
        System.out.println("DB COUNT(" + eventKey + ") = " + savedCount);

        assertThat(savedCount).isEqualTo(100L);
    }
}
