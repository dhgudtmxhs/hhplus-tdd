# 항해플러스 1주차 - TDD
이번 1주차에서는 요구사항 TODO에 대해 TDD를 적용하여 개발을 진행하고, 동시성 제어 문제를 해결했습니다.  
README에서는 동시성 제어에 대한 분석을 다루고 있으며, 전체적인 설명은 PR 템플릿에서 확인할 수 있습니다.

## 동시성 제어

### 동시성 문제란?
**여러 스레드**가 동시에 같은 자원에 접근하려 할 때 발생하는 문제로, **데이터 불일치**나 **경쟁 상태(race condition)** 등을 초래할 수 있습니다.  
예를 들어 두 개의 요청이 동시에 들어와 사용자의 포인트를 충전하려고 할 때, 둘 다 동일한 초기 포인트 잔액을 참조한 뒤 동시에 충전이 이루어지면, 최종 잔액이 올바르지 않게 될 수 있습니다.  
이러한 문제를 해결하기 위해서는 각 요청이 순차적으로 처리되도록 보장해야 합니다.  
> 수학적으로 '동시'라는 표현은 사용될 수 없지만, 하나의 요청이 처리되기 전에 다른 요청이 겹쳐서 들어오는 상황으로 이해할 수 있습니다.

### 요구사항
동시에 여러 건의 포인트 충전 및 이용 요청이 들어올 경우, 각 요청은 순차적으로 처리되어야 합니다.

### 이해
각 사용자의 요청은 **다른 사용자와 구분되도록** 처리되어야 하며, **동일한 사용자에 대한 요청은 순차적으로 처리**되어야 합니다.  
> 만약 다른 사용자와 구분되지 않는다면, 다른 사용자의 요청이 처리되는 동안 내 요청도 대기하게 될 수 있습니다.

---

### `synchronized` vs `ReentrantLock`

### 1. **`synchronized`**

- **메서드 및 블록 레벨에서 동기화 가능**: `synchronized`는 메서드나 코드 블록 단위로 락을 걸 수 있습니다. 메서드 전체에 락을 걸 경우, 메서드 내의 모든 동기화 작업이 동일한 락을 공유하게 되어 불필요한 락 경합이 발생할 수 있습니다.

- **순차 보장 불가능**: `synchronized`는 **기본적으로 공정성(fairness)을 보장하지 않습니다**. 즉, 여러 스레드가 동시에 락을 요청할 경우, 먼저 요청한 스레드가 반드시 먼저 락을 획득한다는 보장이 없습니다. 이는 순차적인 처리가 필요한 경우에 문제가 될 수 있습니다.

- **재진입 제어 불가**: `synchronized`는 동일 스레드가 락을 여러 번 획득할 수 있지만, 이 경우 다른 스레드는 락을 기다리게 되어 교착 상태나 성능 저하가 발생할 수 있습니다.

- **타임아웃 및 대기 시간 설정 불가**: `synchronized`는 락을 획득하는 동안 **무조건 대기**하게 됩니다. 즉, 타임아웃같이 일정 시간 후 락을 포기하는 등의 유연한 동기화 처리가 불가능합니다. 이러한 유연성이 필요한 경우 **`ReentrantLock`**을 사용하는 것이 유리합니다.

### 2. **`ReentrantLock`**

- **세밀한 제어 가능**: `ReentrantLock`은 특정 코드 블록에 대해서만 락을 걸 수 있기 때문에 **메서드 전체가 아닌, 필요한 부분에만 락을 적용**하여 성능을 최적화할 수 있습니다. 이를 통해 더 세밀한 동기화 제어가 가능합니다.

- **공정한 락 관리 (Fair Lock)**: `ReentrantLock`은 **공정락(Fair Lock)**을 지원합니다. 이를 통해 **먼저 요청한 스레드가 먼저 락을 획득**하도록 보장할 수 있습니다. 따라서 여러 요청이 동시에 들어오는 상황에서 **순차적인 처리**가 필요할 때 적합합니다.

- **재진입락 (Reentrant Lock)**: `ReentrantLock`은 재진입락을 지원합니다. 동일한 스레드가 락을 여러 번 획득할 수 있으며, 다른 스레드는 그 동안 대기하게 됩니다. 이로 인해 **교착 상태**를 피할 수 있고, 복잡한 동기화 로직을 효율적으로 처리할 수 있습니다.

- **타임아웃 및 대기 시간 설정 가능**: `ReentrantLock`은 `tryLock()` 메서드를 통해 **타임아웃**을 설정하거나 **일정 시간 동안만 락**을 시도할 수 있습니다. 이로 인해 락을 획득하지 못할 경우 포기하거나 대체 작업을 수행할 수 있습니다. 이러한 유연한 제어는 **교착 상태**를 방지하고 시스템의 응답성을 높이는 데 도움이 됩니다.

---
### ReentrantLock: 공정락 (Fair Lock) 과 비공정락 (Non-Fair Lock)

Java의 `ReentrantLock`은 공정락(Fair Lock)과 비공정락(Non-Fair Lock)정책을 제공합니다. 두 락 정책의 주요 차이점은 다음과 같습니다:

| **특징**                | **공정락 (Fair Lock)**                      | **비공정락 (Non-Fair Lock)**                |
|-------------------------|---------------------------------------------|---------------------------------------------|
| **락 획득 순서**         | 요청한 순서대로 (FIFO 방식)                  | 임의의 순서 (경쟁적으로)                     |
| **공정성 보장**          | 보장됨                                       | 보장되지 않음                                 |
| **처리량 (Throughput)** | 상대적으로 낮음                               | 상대적으로 높음                               |
| **락 획득 지연**          | 락 획득에 시간이 더 걸릴 수 있음               | 락을 더 빨리 획득할 수 있음                   |
| **스타베이션 위험**      | 낮음                                         | 높음                                         |
| **적합한 사용 사례**      | 모든 스레드가 공평하게 락을 획득해야 할 때     | 성능이 중요한 경우, 공정성이 덜 중요한 경우  |

#### **공정락 (Fair Lock)**
- **장점**:
  - 모든 스레드가 공평하게 락을 획득할 수 있어 스타베이션이 발생하지 않습니다.
  - 시스템의 예측 가능성이 높아집니다.
  
- **단점**:
  - 락 획득에 시간이 더 걸릴 수 있어 전체적인 처리량이 낮아질 수 있습니다.
  - 높은 공정성을 유지하기 위해 스레드 간의 컨텍스트 스위칭이 증가할 수 있습니다.

#### **비공정락 (Non-Fair Lock)**
- **장점**:
  - 락을 더 빠르게 획득할 수 있어 처리량이 증가합니다.
  - 시스템의 전반적인 성능이 향상될 수 있습니다.
  
- **단점**:
  - 특정 스레드가 지속적으로 락을 획득하여 다른 스레드가 락을 획득하지 못할 가능성이 있습니다 (스타베이션).
  - 락 획득 순서가 예측 불가능하여 공정성이 요구되는 상황에 부적합할 수 있습니다.

---

### 메서드
```java
    public UserPoint chargeUserPoint(long id, long amount) {
        ReentrantLock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try {
            UserPoint userPoint = userPointTable.selectById(id);
            UserPoint updatedPoint = userPoint.charge(amount);

            userPointTable.insertOrUpdate(id, updatedPoint.point());
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updatedPoint.updateMillis());
            return updatedPoint;
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                userLocks.remove(id, lock);
            }
        }
    }

    public UserPoint useUserPoint(long id, long amount) {
        ReentrantLock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try {
            UserPoint userPoint = userPointTable.selectById(id);
            UserPoint updatedPoint = userPoint.use(amount);
            userPointTable.insertOrUpdate(id, updatedPoint.point());
            pointHistoryTable.insert(id, -amount, TransactionType.USE, updatedPoint.updateMillis());
            return updatedPoint;
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                userLocks.remove(id, lock);
            }
        }
    }
```
### 주요 구성 요소

- **ReentrantLock:**  
  동시성 제어를 위해 사용되며, 동일 유저에 대한 동시 접근을 방지하여 데이터의 일관성을 유지합니다.

- **userLocks 맵:**  
  유저별 잠금을 관리하는 맵으로, 유저 ID를 키로 하고 `ReentrantLock`을 값으로 가집니다.

- **userPointTable:**  
  유저의 포인트 정보를 조회하고 업데이트하는 데이터베이스 테이블을 나타냅니다.

- **pointHistoryTable:**  
  포인트 변경 내역을 기록하는 테이블로, 추후 분석이나 기록 조회에 사용됩니다.

---

### 테스트
```java
@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("여러 사용자가 동시에 요청할 때 동일 사용자에 대한 충전/사용 작업은 순차적으로 처리된다.")
    void concurrentChargeAndUseMultiUsersTest() throws InterruptedException {
        // Given
        final Long userId1 = 1L;
        final Long userId2 = 2L;
        final Long userId3 = 3L;
        final int threadCount = 30; // 작업 수
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 쓰레드 풀 설정
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 카운트 설정

        // When: 각 10개의 충전 및 사용
        for (int i = 0; i < threadCount; i++) {
            final Long userId;
            if (i % 3 == 0) {
                userId = userId1;
            } else if (i % 3 == 1) {
                userId = userId2;
            } else {
                userId = userId3;
            }

            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, 1000L);
                    pointService.useUserPoint(userId, 400L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();

        // Then
        UserPoint finalUserPoint1 = pointService.getUserPoint(userId1);
        assertThat(finalUserPoint1.point()).isEqualTo(6000L);

        UserPoint finalUserPoint2 = pointService.getUserPoint(userId2);
        assertThat(finalUserPoint2.point()).isEqualTo(6000L);

        UserPoint finalUserPoint3 = pointService.getUserPoint(userId3);
        assertThat(finalUserPoint3.point()).isEqualTo(6000L);
    }

}

