# Optimistic Locking Issue Analysis

## Problem Summary
The `EventConcurrencyTest` was not triggering an optimistic lock conflict even though the code has proper support for it (`@Version`, `saveAndFlush()`, Spring `@Transactional`).

## Root Cause Analysis

### 1. **Test Synchronization Issue (PRIMARY)**
**Current Problem:**
```java
CountDownLatch startLatch = new CountDownLatch(1);
startLatch.countDown();  // Both threads start almost immediately
```

- Only ensures both threads START, NOT that both have READ the entity before either WRITES
- Thread scheduling allows one thread to complete read→write before the other thread reads
- Result: Second thread reads AFTER first commit, so no version conflict

**What Should Happen for Optimistic Lock to Fire:**
1. Thread A: SELECT (gets version V0)
2. Thread B: SELECT (gets version V0)
3. Thread A: UPDATE ... WHERE id=? AND version=0 → SUCCESS (version becomes V1)
4. Thread B: UPDATE ... WHERE id=? AND version=0 → **FAILS** (0 rows affected, no row matches version=0)
5. Hibernate throws `ObjectOptimisticLockingFailureException`

### 2. **Code Implementation (CORRECT)**
✓ Entity has `@Version` annotation
✓ Service uses `@Transactional` (Spring-managed)
✓ Service uses `saveAndFlush()` (forces immediate UPDATE with version check)
✓ GlobalExceptionHandler catches `ObjectOptimisticLockingFailureException`

### 3. **Test Issues**
- Uses `EventService.updateEvent()` which hides the transaction management from the test
- Cannot precisely control when READ vs WRITE happens in each thread
- No visibility into versions being read/written
- `startLatch` provides insufficient synchronization

## Solution
Replace single `startLatch` with **two-phase synchronization**:
- **Phase 1:** readLatch (2 threads must READ entity)
- **Phase 2:** updateLatch (then release both to UPDATE simultaneously)

Use `TransactionTemplate` + `EventRepository` directly in test to have fine-grained control over transaction flow.

## Expected Test Behavior After Fix
```
[Thread-A] READ completed - Event version: 0
[Thread-B] READ completed - Event version: 0
[Thread-A] About to saveAndFlush - version before: 0
[Thread-B] About to saveAndFlush - version before: 0
[Thread-A] saveAndFlush completed successfully - version after: 1
[Thread-B] EXCEPTION: ObjectOptimisticLockingFailureException
  Message: Row was updated or deleted by another transaction
```

## Files to Check
1. ✓ `/entity/Event.java` - Has @Version
2. ✓ `/service/EventServiceImpl.java` - Uses saveAndFlush()
3. ✓ `/exception/GlobalExceptionHandler.java` - Catches optimistic lock exception
4. ✗ `/test/EventConcurrencyTest.java` - **TEST IS THE ISSUE**

## Database Config
✓ InnoDB (supports transactions)
✓ REPEATABLE_READ isolation level (from logs)
✓ version column created automatically by JPA
