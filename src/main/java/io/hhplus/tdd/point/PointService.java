package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private static final long MAX_POINT = 1_000_000L; // 최대 잔고

    /**
     * 특정 유저의 포인트 정보 조회
     */
    public UserPoint getUserPoint(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역 조회
     */
    public List<PointHistory> getUserPointHistories(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * 특정 유저의 포인트 충전
     */
    public UserPoint chargeUserPoint(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        UserPoint updatedPoint = userPoint.charge(amount);

        userPointTable.insertOrUpdate(id, updatedPoint.point());
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updatedPoint.updateMillis());
        return updatedPoint;
    }

    /**
     * 특정 유저의 포인트 사용
     */
    public UserPoint useUserPoint(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);
        UserPoint updatedPoint = userPoint.use(amount);

        userPointTable.insertOrUpdate(id, updatedPoint.point());
        pointHistoryTable.insert(id, -amount, TransactionType.USE, updatedPoint.updateMillis());
        return updatedPoint;
    }

}
