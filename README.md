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
>
> 
---


### `synchronized` vs `ReentrantLock`

### 1. **`synchronized`**

- **메서드 및 블록 레벨에서 동기화 가능**: synchronized는 메서드나 코드 블록 단위로 락을 걸 수 있습니다. 메서드 전체에 락을 걸 경우, 메서드 내의 모든 동기화 작업이 동일한 락을 공유하게 되어 불필요한 락 경합이 발생할 수 있습니다.

- **순차 보장 불가능**: `synchronized`는 **기본적으로 공정성(fairness)을 보장하지 않습니다**. 즉, 여러 스레드가 동시에 락을 요청할 경우, 먼저 요청한 스레드가 반드시 먼저 락을 획득한다는 보장이 없습니다. 이는 순차적인 처리가 필요한 경우에 문제가 될 수 있습니다.

- **재진입 제어 불가**: `synchronized`는 동일 스레드가 락을 여러 번 획득할 수 있지만, 이 경우 다른 스레드는 락을 기다리게 되어 교착 상태나 성능 저하가 발생할 수 있습니다.

- **타임아웃 및 대기 시간 설정 불가**: `synchronized`는 락을 획득하는 동안 **무조건 대기**하게 됩니다. 즉, 타임아웃같이 일정 시간 후 락을 포기하는 등의 유연한 동기화 처리가 불가능합니다. 이러한 유연성이 필요한 경우 **`ReentrantLock`**을 사용하는 것이 유리합니다.

### 2. **`ReentrantLock`**

- **세밀한 제어 가능**: `ReentrantLock`은 **특정 코드 블록**에 대해서만 락을 걸 수 있기 때문에 **메서드 전체가 아닌, 필요한 부분에만 락을 적용**하여 성능을 최적화할 수 있습니다. 이를 통해 **더 세밀한 동기화 제어**가 가능합니다.

- **공정한 락 관리 (Fair Lock)**: `ReentrantLock`은 **공정락(Fair Lock)**을 지원합니다. 이를 통해 **먼저 요청한 스레드**가 **먼저 락을 획득**하도록 보장할 수 있습니다. 따라서 여러 요청이 동시에 들어오는 상황에서 **순차적인 처리**가 필요할 때 적합합니다.

- **재진입락 (Reentrant Lock)**: `ReentrantLock`은 **재진입락**을 지원합니다. 동일한 스레드가 락을 여러 번 획득할 수 있으며, 다른 스레드는 그 동안 대기하게 됩니다. 이로 인해 **교착 상태**를 피할 수 있고, **복잡한 동기화 로직을 효율적으로 처리**할 수 있습니다.

- **타임아웃 및 대기 시간 설정 가능**: `ReentrantLock`은 `tryLock()` 메서드를 통해 **타임아웃**을 설정하거나 **일정 시간** 동안만 락을 시도할 수 있습니다. 이로 인해 락을 획득하지 못할 경우 **포기하거나 대체 작업**을 수행할 수 있습니다. 이러한 유연한 제어는 **교착 상태**를 방지하고 시스템의 응답성을 높이는 데 도움이 됩니다.


### 요약
`synchronized`는 **메서드 단위**로 동기화를 처리하기 때문에 세밀한 제어가 어렵고, **공정성(fairness)**을 보장하지 않으며, **`ReentrantLock`**보다 유연한 제어가 어렵습니다. 또한, **타임아웃 설정** 등의 추가 기능이 없어 **순차 처리가 중요한** 경우 적합하지 않을 수 있습니다.
  
`ReentrantLock`은 **세밀한 제어**가 가능하고, **공정락**을 통해 락을 요청한 순서대로 처리되도록 보장하며, **재진입락**을 통해 동기화가 더 유연하게 이루어집니다. 또한 **타임아웃**이나 **대기 시간 설정** 등 다양한 기능을 제공하여 복잡한 동기화가 필요한 상황에서 유리합니다.


---


### 공정락 (Fair Lock)과 비공정락 (Unfair Lock)의 차이

#### 공정락 (Fair Lock)

- **공정락**은 락을 요청한 순서대로 락을 획득하는 방식입니다. 즉, 먼저 락을 요청한 스레드가 먼저 락을 획득하도록 보장합니다.
- 이는 대기 중인 스레드가 있으면, 항상 대기 중인 스레드가 선순위로 락을 획득할 수 있게 하여 **교착 상태**를 방지하고, **공정한 자원 할당**을 제공합니다.
- 예를 들어, **ReentrantLock**을 사용할 때 **공정락**을 설정하려면, `new ReentrantLock(true)`와 같이 `true` 값을 설정합니다.

#### 비공정락 (Unfair Lock)

- **비공정락**은 락을 요청한 순서와 상관없이 락을 획득할 수 있습니다. 즉, 먼저 요청한 스레드가 반드시 먼저 락을 획득한다는 보장이 없습니다.
- 락을 요청한 스레드 중에서 일부 스레드가 우선적으로 락을 획득할 수 있기 때문에, **교착 상태**나 **성능이 더 우선**하는 경우에 유리할 수 있습니다.
- 기본적으로 **ReentrantLock**은 비공정락을 사용하며, `new ReentrantLock(false)`와 같이 `false` 값을 설정합니다.

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
-  **`ConcurrentMap`**을 사용하여 **`ReentrantLock`** 객체를 관리하여 사용자별로 처리합니다.

1. **락 객체 생성 및 관리**:
   - `ReentrantLock lock = userLocks.computeIfAbsent(id, k -> new ReentrantLock(true));`
     - `userLocks`는 각 **사용자 ID**에 대해 **`ReentrantLock`**을 관리하는 **맵**입니다.
     - **`computeIfAbsent`**는 `id`에 해당하는 락이 없으면 새로 생성하여 추가합니다.
     - `true`는 **공정락(Fair Lock)**을 설정하여 **먼저 요청한 스레드**가 **먼저 락을 획득**하도록 보장합니다.

2. **락을 걸고 작업 시작**:
   - `lock.lock();`을 사용하여 **락을 걸고**, 충전 작업을 진행합니다.
   - **포인트 충전** 후, **업데이트된 포인트**를 데이터베이스에 반영하고, **포인트 충전 내역**을 히스토리 테이블에 기록합니다.

3. **작업 후 락 해제**:
   - `lock.unlock();`을 호출하여 **락을 해제**합니다.
   - `if (!lock.hasQueuedThreads())`는 락을 기다리고 있는 다른 스레드가 없다면 해당 락을 **맵에서 제거**합니다. 이를 통해 **메모리 효율성**을 높입니다.
  


