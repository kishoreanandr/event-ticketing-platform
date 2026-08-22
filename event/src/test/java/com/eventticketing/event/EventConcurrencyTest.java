package com.eventticketing.event;

import com.eventticketing.event.entity.Event;
import com.eventticketing.event.entity.EventStatus;
import com.eventticketing.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EventConcurrencyTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldAllowOnlyOneConcurrentUpdate() throws Exception {

        // ---------------------------------------------------------
        // 1. Create a fresh event for this test
        // ---------------------------------------------------------

        Event event = new Event();

        event.setName("Initial Event");
        event.setDescription("Initial Description");
        event.setVenue("Initial Venue");
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setTotalSeats(500);
        event.setAvailableSeats(500);
        event.setTicketPrice(new BigDecimal("100.00"));
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizerId(25L);

        Event savedEvent = eventRepository.saveAndFlush(event);

        Long eventId = savedEvent.getId();

        System.out.println("==========================================");
        System.out.println("TEST EVENT");
        System.out.println("ID      : " + eventId);
        System.out.println("VERSION : " + savedEvent.getVersion());
        System.out.println("==========================================");


        // ---------------------------------------------------------
        // 2. Synchronization
        // ---------------------------------------------------------

        /*
         * Both threads must complete their READ
         * before either thread is allowed to UPDATE.
         */
        CountDownLatch readComplete = new CountDownLatch(2);

        /*
         * Main thread uses this latch to release both
         * transactions after both have read the entity.
         */
        CountDownLatch updateRelease = new CountDownLatch(1);


        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        ConcurrentLinkedQueue<Throwable> exceptions =
                new ConcurrentLinkedQueue<>();


        // ---------------------------------------------------------
        // 3. Thread A
        // ---------------------------------------------------------

        Runnable taskA = () -> {

            System.out.println("[A] Starting");

            try {

                transactionTemplate.execute(transactionStatus -> {

                    try {

                        // READ
                        Event eventA = eventRepository
                                .findById(eventId)
                                .orElseThrow();

                        System.out.println(
                                "[A] Read version = "
                                        + eventA.getVersion()
                        );

                        // Tell the other thread:
                        // "I have finished reading"
                        readComplete.countDown();


                        // Wait until BOTH threads have read
                        readComplete.await(
                                10,
                                TimeUnit.SECONDS
                        );

                        System.out.println(
                                "[A] Both threads have read"
                        );


                        // Wait for main thread to release UPDATE
                        updateRelease.await(
                                10,
                                TimeUnit.SECONDS
                        );


                        // MODIFY
                        eventA.setName("Conference A");
                        eventA.setDescription("Updated by A");
                        eventA.setVenue("Bangalore");
                        eventA.setEventDate(
                                LocalDateTime.of(
                                        2026,
                                        10,
                                        15,
                                        10,
                                        0
                                )
                        );
                        eventA.setTotalSeats(600);
                        eventA.setTicketPrice(
                                new BigDecimal("1299.00")
                        );


                        // UPDATE
                        System.out.println(
                                "[A] Attempting update with version = "
                                        + eventA.getVersion()
                        );

                        eventRepository.saveAndFlush(eventA);

                        System.out.println(
                                "[A] UPDATE SUCCESS. New version = "
                                        + eventA.getVersion()
                        );

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();

                        throw new RuntimeException(e);
                    }

                    return null;
                });

            } catch (Throwable e) {

                exceptions.add(e);

                System.out.println(
                        "[A] FAILED: "
                                + e.getClass().getSimpleName()
                );

                System.out.println(
                        "[A] MESSAGE: "
                                + e.getMessage()
                );
            }
        };


        // ---------------------------------------------------------
        // 4. Thread B
        // ---------------------------------------------------------

        Runnable taskB = () -> {

            System.out.println("[B] Starting");

            try {

                transactionTemplate.execute(transactionStatus -> {

                    try {

                        // READ
                        Event eventB = eventRepository
                                .findById(eventId)
                                .orElseThrow();

                        System.out.println(
                                "[B] Read version = "
                                        + eventB.getVersion()
                        );

                        // Tell the other thread:
                        // "I have finished reading"
                        readComplete.countDown();


                        // Wait until BOTH threads have read
                        readComplete.await(
                                10,
                                TimeUnit.SECONDS
                        );

                        System.out.println(
                                "[B] Both threads have read"
                        );


                        // Wait for main thread to release UPDATE
                        updateRelease.await(
                                10,
                                TimeUnit.SECONDS
                        );


                        // MODIFY
                        eventB.setName("Conference B");
                        eventB.setDescription("Updated by B");
                        eventB.setVenue("Chennai");
                        eventB.setEventDate(
                                LocalDateTime.of(
                                        2026,
                                        10,
                                        20,
                                        10,
                                        0
                                )
                        );
                        eventB.setTotalSeats(700);
                        eventB.setTicketPrice(
                                new BigDecimal("1499.00")
                        );


                        // UPDATE
                        System.out.println(
                                "[B] Attempting update with version = "
                                        + eventB.getVersion()
                        );

                        eventRepository.saveAndFlush(eventB);

                        System.out.println(
                                "[B] UPDATE SUCCESS. New version = "
                                        + eventB.getVersion()
                        );

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();

                        throw new RuntimeException(e);
                    }

                    return null;
                });

            } catch (Throwable e) {

                exceptions.add(e);

                System.out.println(
                        "[B] FAILED: "
                                + e.getClass().getSimpleName()
                );

                System.out.println(
                        "[B] MESSAGE: "
                                + e.getMessage()
                );
            }
        };


        // ---------------------------------------------------------
        // 5. Start both threads
        // ---------------------------------------------------------

        Future<?> futureA = executor.submit(taskA);
        Future<?> futureB = executor.submit(taskB);


        // ---------------------------------------------------------
        // 6. Wait until BOTH have completed READ
        // ---------------------------------------------------------

        boolean bothRead = readComplete.await(
                10,
                TimeUnit.SECONDS
        );

        assertTrue(
                bothRead,
                "Both threads did not complete the READ phase"
        );


        System.out.println(
                "\nBoth threads have read the same version."
        );

        System.out.println(
                "Releasing both threads for UPDATE...\n"
        );


        // ---------------------------------------------------------
        // 7. Release both threads
        // ---------------------------------------------------------

        updateRelease.countDown();


        // ---------------------------------------------------------
        // 8. Wait for both threads
        // ---------------------------------------------------------

        futureA.get(15, TimeUnit.SECONDS);
        futureB.get(15, TimeUnit.SECONDS);

        executor.shutdown();


        // ---------------------------------------------------------
        // 9. Verify result
        // ---------------------------------------------------------

        System.out.println("\n==========================================");
        System.out.println("TEST RESULT");
        System.out.println("==========================================");

        System.out.println(
                "Number of exceptions = "
                        + exceptions.size()
        );


        for (Throwable exception : exceptions) {

            System.out.println(
                    exception.getClass().getName()
            );

            System.out.println(
                    exception.getMessage()
            );
        }


        boolean optimisticLockOccurred =
                exceptions.stream()
                        .anyMatch(
                                this::isOptimisticLockException
                        );


        // ---------------------------------------------------------
        // 10. Verify final database state
        // ---------------------------------------------------------

        Event finalEvent =
                eventRepository
                        .findById(eventId)
                        .orElseThrow();

        System.out.println(
                "\nFinal Event Name = "
                        + finalEvent.getName()
        );

        System.out.println(
                "Final Version = "
                        + finalEvent.getVersion()
        );


        // ---------------------------------------------------------
        // 11. Final assertion
        // ---------------------------------------------------------

        assertTrue(
                optimisticLockOccurred,
                "Expected optimistic locking exception"
        );

        System.out.println(
                "\n✓ OPTIMISTIC LOCKING TEST PASSED"
        );
    }


    private boolean isOptimisticLockException(
            Throwable throwable) {

        Throwable current = throwable;

        while (current != null) {

            if (current instanceof
                    ObjectOptimisticLockingFailureException) {

                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}