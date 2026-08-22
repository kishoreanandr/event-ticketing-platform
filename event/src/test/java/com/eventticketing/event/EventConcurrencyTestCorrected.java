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

/**
 * CORRECTED Optimistic Locking Test
 * 
 * This test properly synchronizes two threads to force an optimistic lock conflict.
 * 
 * WHAT HAPPENS:
 * 1. Thread-A reads entity at version 0
 * 2. Thread-B reads entity at version 0 (both have same version)
 * 3. Thread-A updates and flushes (version becomes 1)
 * 4. Thread-B tries to update with version 0 (fails - row already at version 1)
 * 5. Hibernate throws ObjectOptimisticLockingFailureException
 */
@SpringBootTest
public class EventConcurrencyTestCorrected {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private PlatformTransactionManager txManager;

    @Test
    void testConcurrentUpdates_MustTriggerOptimisticLock() throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("OPTIMISTIC LOCKING TEST - CORRECTED VERSION");
        System.out.println("=".repeat(80));

        // SETUP: Create test event
        Event testEvent = new Event();
        testEvent.setName("Initial Event");
        testEvent.setDescription("Initial");
        testEvent.setVenue("Initial Venue");
        testEvent.setEventDate(LocalDateTime.now().plusDays(1));
        testEvent.setTotalSeats(500);
        testEvent.setAvailableSeats(500);
        testEvent.setTicketPrice(BigDecimal.valueOf(100));
        testEvent.setOrganizerId(25L);
        testEvent.setStatus(EventStatus.DRAFT);
        
        Event saved = eventRepository.saveAndFlush(testEvent);
        Long eventId = saved.getId();

        System.out.println("\nInitial Setup:");
        System.out.println("  Event ID: " + eventId);
        System.out.println("  Initial Version: " + saved.getVersion());

        // TWO-PHASE SYNCHRONIZATION
        CountDownLatch readPhaseComplete = new CountDownLatch(2);   // Phase 1: Both must READ
        CountDownLatch updatePhaseRelease = new CountDownLatch(1);  // Phase 2: Release to UPDATE

        ExecutorService executor = Executors.newFixedThreadPool(2);
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        ConcurrentLinkedQueue<Throwable> exceptionQueue = new ConcurrentLinkedQueue<>();

        // THREAD A
        Runnable threadA = () -> {
            System.out.println("\n[THREAD-A] Starting...");
            try {
                txTemplate.execute(txStatus -> {
                    try {
                        // PHASE 1: READ
                        System.out.println("[THREAD-A]   → SELECT entity from database");
                        Event event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new RuntimeException("Event not found"));
                        long versionRead = event.getVersion();
                        System.out.println("[THREAD-A]   ✓ READ complete, version=" + versionRead);

                        // Signal that this thread has read
                        readPhaseComplete.countDown();
                        System.out.println("[THREAD-A]   ⏳ Waiting for THREAD-B to read...");
                        
                        // Wait for both threads to complete reads
                        if (!readPhaseComplete.await(10, TimeUnit.SECONDS)) {
                            throw new RuntimeException("Timeout waiting for both threads to read");
                        }
                        System.out.println("[THREAD-A]   ✓ Both threads have READ");

                        // PHASE 2: WAIT FOR RELEASE, THEN UPDATE
                        System.out.println("[THREAD-A]   ⏳ Waiting for release to proceed with UPDATE...");
                        if (!updatePhaseRelease.await(10, TimeUnit.SECONDS)) {
                            throw new RuntimeException("Timeout waiting for update release");
                        }
                        System.out.println("[THREAD-A]   ✓ Release signal received");

                        // Modify entity
                        System.out.println("[THREAD-A]   → Modifying entity...");
                        event.setName("Conference A");
                        event.setDescription("Updated by A");
                        event.setVenue("Bangalore");
                        event.setEventDate(LocalDateTime.of(2026, 10, 15, 10, 0));
                        event.setTotalSeats(600);
                        event.setTicketPrice(BigDecimal.valueOf(1299));

                        // UPDATE (with version check)
                        System.out.println("[THREAD-A]   → Executing: UPDATE events SET ... WHERE id=" 
                            + eventId + " AND version=" + versionRead);
                        eventRepository.saveAndFlush(event);
                        System.out.println("[THREAD-A]   ✓ UPDATE SUCCESS! New version=" + event.getVersion());

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted", e);
                    }
                    return null;
                });
            } catch (Throwable e) {
                exceptionQueue.offer(e);
                System.out.println("[THREAD-A]   ✗ EXCEPTION: " + e.getClass().getSimpleName());
                System.out.println("[THREAD-A]      " + e.getMessage());
            }
        };

        // THREAD B
        Runnable threadB = () -> {
            System.out.println("\n[THREAD-B] Starting...");
            try {
                txTemplate.execute(txStatus -> {
                    try {
                        // PHASE 1: READ
                        System.out.println("[THREAD-B]   → SELECT entity from database");
                        Event event = eventRepository.findById(eventId)
                            .orElseThrow(() -> new RuntimeException("Event not found"));
                        long versionRead = event.getVersion();
                        System.out.println("[THREAD-B]   ✓ READ complete, version=" + versionRead);

                        // Signal that this thread has read
                        readPhaseComplete.countDown();
                        System.out.println("[THREAD-B]   ⏳ Waiting for THREAD-A to read...");
                        
                        // Wait for both threads to complete reads
                        if (!readPhaseComplete.await(10, TimeUnit.SECONDS)) {
                            throw new RuntimeException("Timeout waiting for both threads to read");
                        }
                        System.out.println("[THREAD-B]   ✓ Both threads have READ");

                        // PHASE 2: WAIT FOR RELEASE, THEN UPDATE
                        System.out.println("[THREAD-B]   ⏳ Waiting for release to proceed with UPDATE...");
                        if (!updatePhaseRelease.await(10, TimeUnit.SECONDS)) {
                            throw new RuntimeException("Timeout waiting for update release");
                        }
                        System.out.println("[THREAD-B]   ✓ Release signal received");

                        // Modify entity
                        System.out.println("[THREAD-B]   → Modifying entity...");
                        event.setName("Conference B");
                        event.setDescription("Updated by B");
                        event.setVenue("Chennai");
                        event.setEventDate(LocalDateTime.of(2026, 10, 20, 10, 0));
                        event.setTotalSeats(700);
                        event.setTicketPrice(BigDecimal.valueOf(1499));

                        // UPDATE (with version check)
                        System.out.println("[THREAD-B]   → Executing: UPDATE events SET ... WHERE id=" 
                            + eventId + " AND version=" + versionRead);
                        eventRepository.saveAndFlush(event);
                        System.out.println("[THREAD-B]   ✓ UPDATE SUCCESS! New version=" + event.getVersion());

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted", e);
                    }
                    return null;
                });
            } catch (Throwable e) {
                exceptionQueue.offer(e);
                System.out.println("[THREAD-B]   ✗ EXCEPTION: " + e.getClass().getSimpleName());
                System.out.println("[THREAD-B]      " + e.getMessage());
            }
        };

        // EXECUTE THREADS
        System.out.println("\n" + "-".repeat(80));
        System.out.println("PHASE 1: Threads read entity simultaneously");
        System.out.println("-".repeat(80));
        
        Future<?> futureA = executor.submit(threadA);
        Future<?> futureB = executor.submit(threadB);

        // Wait for both to complete reads
        System.out.println("[MAIN] Waiting for both threads to complete READ phase...");
        readPhaseComplete.await(10, TimeUnit.SECONDS);

        System.out.println("\n" + "-".repeat(80));
        System.out.println("PHASE 2: Threads attempt update simultaneously");
        System.out.println("-".repeat(80) + "\n");
        
        updatePhaseRelease.countDown();

        // Wait for completion
        try {
            futureA.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            // Already captured in exceptionQueue
        }
        
        try {
            futureB.get(15, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            // Already captured in exceptionQueue
        }

        executor.shutdown();

        // RESULTS
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST RESULTS");
        System.out.println("=".repeat(80));
        
        System.out.println("Exceptions caught: " + exceptionQueue.size());
        for (Throwable t : exceptionQueue) {
            System.out.println("  ✗ " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }

        // Check for optimistic lock exception
        boolean hasOptimisticLock = exceptionQueue.stream()
            .anyMatch(e -> isOptimisticLockException(e));

        // Check DB final state
        Event finalEvent = eventRepository.findById(eventId).orElseThrow();
        System.out.println("\nFinal Database State:");
        System.out.println("  Name: " + finalEvent.getName());
        System.out.println("  Version: " + finalEvent.getVersion());

        System.out.println("\n" + "=".repeat(80));
        if (hasOptimisticLock) {
            System.out.println("✓✓✓ TEST PASSED ✓✓✓");
            System.out.println("Optimistic lock conflict detected as expected!");
            System.out.println("One thread successfully updated, the other failed with:");
            System.out.println("ObjectOptimisticLockingFailureException");
        } else {
            System.out.println("✗✗✗ TEST FAILED ✗✗✗");
            System.out.println("Expected an optimistic lock conflict but both updates succeeded!");
            System.out.println("This means the synchronization is not working correctly.");
        }
        System.out.println("=".repeat(80) + "\n");

        assertTrue(hasOptimisticLock, 
            "Expected ObjectOptimisticLockingFailureException but caught: " + exceptionQueue);
    }

    /**
     * Check if exception is an optimistic lock exception (direct or wrapped)
     */
    private boolean isOptimisticLockException(Throwable t) {
        if (t instanceof ObjectOptimisticLockingFailureException) {
            return true;
        }
        if (t.getCause() instanceof ObjectOptimisticLockingFailureException) {
            return true;
        }
        // Check by class name (in case of wrapped exceptions)
        return t.getClass().getSimpleName().contains("OptimisticLocking");
    }
}
