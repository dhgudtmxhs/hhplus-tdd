package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final ConcurrentMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    /**
     * 특정 유저의 포인트 정보 조회
     */
    public UserPoint getUserPoint(long id) {
        UserPoint.validateId(id);
        return userPointTable.selectById(id);
    }
    /**
     * 특정 유저의 포인트 충전/이용 내역 조회
     */
    public List<PointHistory> getUserPointHistories(long id) {
        UserPoint.validateId(id);
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * 특정 유저의 포인트 충전
     */
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

    /**
     * 특정 유저의 포인트 사용
     */
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

}
