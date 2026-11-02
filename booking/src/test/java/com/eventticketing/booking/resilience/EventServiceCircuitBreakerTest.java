package com.eventticketing.booking.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EventServiceCircuitBreakerTest {

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp(){
        CircuitBreakerConfig config=new CircuitBreakerConfig.Builder()
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(100))
                .permittedNumberOfCallsInHalfOpenState(2)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .build();
        CircuitBreakerRegistry registry=CircuitBreakerRegistry.of(config);
        circuitBreaker=registry.circuitBreaker("event-service-test");
    }

    @Test
    void shouldOpenWhenFailureRateReachesThreshold(){
        recordSuccess();
        recordSuccess();
        recordFailure();
        recordFailure();
        assertEquals(CircuitBreaker.State.OPEN,
                circuitBreaker.getState());
    }

    @Test
    void shouldRejectCallsWhenOpen(){
        recordSuccess();
        recordSuccess();
        recordFailure();
        recordFailure();
        assertEquals(CircuitBreaker.State.OPEN,
                circuitBreaker.getState());
        assertThrows(CallNotPermittedException.class,
                ()->circuitBreaker.executeRunnable(()->{

                }));
    }

    @Test
    void shouldCloseAfterTwoSuccessfulHalfOpenCalls() throws InterruptedException {
        recordSuccess();
        recordSuccess();
        recordFailure();
        recordFailure();
        assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.getState()
        );
        Thread.sleep(150);
        recordSuccess();

        assertEquals(CircuitBreaker.State.HALF_OPEN,
                circuitBreaker.getState());
        recordSuccess();
        assertEquals(CircuitBreaker.State.CLOSED,
                circuitBreaker.getState());
    }


    private void recordSuccess(){
        circuitBreaker.executeRunnable(()->{

        });
    }

    private void recordFailure(){
        assertThrows(
                IllegalStateException.class,
                ()->circuitBreaker.executeRunnable(()->{
                  throw new IllegalStateException("Simulated Failure");
                })
        );
    }

}
