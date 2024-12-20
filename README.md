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

| **특징**           | **`synchronized`**                                                    | **`ReentrantLock`**                                                                                          |
|--------------------|------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| **사용 방식**      | 키워드를 사용하여 메서드 또는 블록 단위로 적용                        | `lock()`과 `unlock()` 메서드를 명시적으로 호출하여 락을 관리                                                |
| **락 관리**        | 자동으로 락을 획득하고 해제                                              | 락을 수동으로 획득하고 해제                                                                                     |
| **공정성 설정**    | 불가능                                                                 | 가능 (`new ReentrantLock(true)`로 공정한 락 생성 가능)                                                        |
| **타임아웃 기능**  | 불가능                                                                 | 가능 (`tryLock(long timeout, TimeUnit unit)` 사용)                                                            |
| **재진입 가능성**  | 동일한 스레드가 이미 락을 획득한 상태에서 재진입 가능                  | 동일한 스레드가 이미 락을 획득한 상태에서 재진입 가능                                                          |
| **조건 변수 지원** | 불가능                                                                 | 가능 (`Condition` 인터페이스 사용)                                                                             |
| **성능**           | 단순한 동기화에서는 일반적으로 더 효율적                               | 고급 기능을 사용할 경우 약간의 오버헤드가 있을 수 있음                                                         |
| **예외 처리**      | JVM이 자동으로 락을 해제                                                | `try-finally` 블록을 사용하여 명시적으로 락을 해제해야 함                                                     |
| **락의 범위**      | 메서드 전체 또는 특정 블록 단위로 제한                                 | 메서드 전체 또는 특정 블록 단위로 유연하게 설정 가능                                                           |
| **사용 사례**      | 간단한 동기화가 필요한 경우                                            | 복잡한 동기화, 공정성 제어, 타임아웃 설정, 조건 변수 사용 등 고급 동시성 제어가 필요한 경우                    |


### 1. **`synchronized`**

- **메서드 및 블록 레벨에서 동기화 가능** : `synchronized`는 메서드나 코드 블록 단위로 락을 걸 수 있습니다. 메서드에 락을 걸 경우, 메서드 내의 모든 동기화 작업이 동일한 락을 공유하게 되어 불필요한 락 경합이 발생할 수 있습니다. 코드 블럭으로 건다 해도, lock을 좀 더 다양하게 관리하거나 복잡한 상황을 컨트롤하기 힘듭니다.

- **순차 보장 불가능** : `synchronized`는 **기본적으로 공정성(fairness)을 보장하지 않습니다**. 즉, 여러 스레드가 동시에 락을 요청할 경우, 먼저 요청한 스레드가 반드시 먼저 락을 획득한다는 보장이 없습니다.

- **재진입 제어 불가** : `synchronized`는 동일 스레드가 락을 여러 번 획득할 수 있지만, 이 경우 다른 스레드는 락을 기다리게 되어 교착 상태나 성능 저하가 발생할 수 있습니다.

- **타임아웃 및 대기 시간 설정 불가** : `synchronized`는 락을 획득하는 동안 **무조건 대기**하게 됩니다. 즉, 타임아웃같이 일정 시간 후 락을 포기하는 등의 유연한 동기화 처리가 불가능합니다. 이러한 유연성이 필요한 경우 **`ReentrantLock`**을 사용하는 것이 유리합니다.

### 2. **`ReentrantLock`**

- **세밀한 제어 가능** : `ReentrantLock`은 메서드 전체나 특정 코드 블록에 락을 걸 수 있으며, 락의 획득과 해제를 명시적으로 제어할 수 있습니다. 이를 통해 필요한 부분에만 락을 적용하거나, 타임아웃, 공정성 설정, 조건 변수 등을 활용하여 더 세밀한 동기화 제어가 가능합니다.

- **공정한 락 관리 (Fair Lock)** : `ReentrantLock`은 **공정락(Fair Lock)**을 지원합니다. 이를 통해 **먼저 요청한 스레드가 먼저 락을 획득**하도록 보장할 수 있습니다. 따라서 여러 요청이 동시에 들어오는 상황에서 **순차적인 처리**가 필요할 때 적합합니다.

- **재진입락 (Reentrant Lock)** : `ReentrantLock`은 재진입락을 지원합니다. 동일한 스레드가 락을 여러 번 획득할 수 있으며, 다른 스레드는 그 동안 대기하게 됩니다. 이로 인해 **교착 상태**를 피할 수 있고, 복잡한 동기화 로직을 효율적으로 처리할 수 있습니다.

- **타임아웃 및 대기 시간 설정 가능** : `ReentrantLock`은 `tryLock()` 메서드를 통해 **타임아웃**을 설정하거나 **일정 시간 동안만 락**을 시도할 수 있습니다. 이로 인해 락을 획득하지 못할 경우 포기하거나 대체 작업을 수행할 수 있습니다. 이러한 유연한 제어는 **교착 상태**를 방지하고 시스템의 응답성을 높이는 데 도움이 됩니다.

---
### ReentrantLock: 공정락 (Fair Lock) 과 비공정락 (Non-Fair Lock)

Java의 `ReentrantLock`은 공정락(Fair Lock)과 비공정락(Non-Fair Lock)정책을 제공합니다. 두 락 정책의 주요 차이점은 다음과 같습니다:

| **특징**                | **공정락 (Fair Lock)**                      | **비공정락 (Non-Fair Lock)**                |
|-------------------------|---------------------------------------------|---------------------------------------------|
| **락 획득 순서**         | 요청한 순서대로 (FIFO 방식)                  | 임의의 순서 (경쟁적으로)                     |
| **공정성 보장**          | 보장됨                                       | 보장되지 않음                                 |
| **처리량**               | 상대적으로 낮음                               | 상대적으로 높음                               |
| **락 획득 지연**          | 락 획득에 시간이 더 걸릴 수 있음               | 락을 더 빨리 획득할 수 있음                   |
| **스타베이션(기아상태) 위험**      | 낮음                                         | 높음                                         |
| **적합한 사용 사례**      | 모든 스레드가 공평하게 락을 획득해야 할 때     |  공정성보다 성능이 더 중요한 경우  |

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
    private final ConcurrentMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

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

- **`ConcurrentMap`** : 여러 스레드가 동시에 접근해도 안전하게 동작하는 맵 인터페이스입니다. 동시성 환경에서 데이터 일관성을 보장하며, `putIfAbsent` 및 `computeIfAbsent` 같은 원자적 연산을 제공합니다. 대표적인 구현체로는 `ConcurrentHashMap`이 있습니다.

- **`ReentrantLock`** : 복잡한 동시성 제어에 유용한 재진입 가능한 락입니다. 공정(fair) 락을 설정할 수 있어 락을 요청한 순서대로 스레드가 락을 획득하도록 할 수 있습니다. `ConcurrentMap`에 ID와 같이 키를 사용하여 각 ID별로 별도의 ReentrantLock을 관리함으로써, 특정 ID에 대한 동시 접근을 효과적으로 제어할 수 있습니다.

- **`computeIfAbsent`** : 특정 키에 대한 값이 존재하지 않을 경우, 제공된 함수에 따라 새로운 값을 생성하고 맵에 추가하는 원자적 연산입니다. 중복 계산을 방지하고 안전하게 값을 초기화할 수 있습니다.

- **`lock()`** : 락을 획득하여 여러 스레드의 동시 접근을 제어합니다. 락을 획득한 스레드만 임계 영역에 접근할 수 있습니다. 만약 락이 이미 다른 스레드에 의해 획득된 상태라면, `lock()`을 호출한 스레드는 락이 해제될 때까지 대기하게 됩니다.

- **`unlock()`** : 락을 해제하여 다른 스레드가 접근할 수 있도록 합니다. finally 블록에서 호출하여 예외가 발생해도 데드락을 방지하고 반드시 락을 해제하도록 보장합니다.

- **`lock.hasQueuedThreads()`** : 락에 대해 대기 중인 스레드가 있는지 확인합니다. 대기 중인 스레드가 없다면, remove 메서드를 통해 맵에서 해당 ID의 락을 제거합니다. 이를 통해 메모리 누수를 방지하고, 불필요한 락 객체가 남아있지 않도록 관리합니다.

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
```

- **`executorService`** : 스레드 풀을 생성하여 동시에 여러 작업을 실행합니다.

- **`countDownLatch`** : 동기화 도구로, 모든 작업이 완료될 때까지 대기합니다.

- **`countDownLatch.countDown()`** : 각 작업이 완료될 때 호출하여 `countDownLatch`의 카운트를 감소시킵니다.

- **`countDownLatch.await();`** : 모든 작업이 완료될 때까지 현재 스레드를 대기시킵니다.

- **`executorService.shutdown()`** : 더 이상 새로운 작업을 받지 않고, 실행 중인 작업이 완료되면 스레드 풀을 종료합니다.

