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
     * 특정 유저의 포인트 충전 조회
     */
    public UserPoint chargeUserPoint(long id, long amount) {
        UserPoint userPoint = userPointTable.selectById(id);

        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원 이하일 수 없습니다.");
        }

        long newPoint = userPoint.point() + amount;

        if (newPoint > MAX_POINT) {
            throw new IllegalArgumentException("포인트 충전은 최대 잔고를 초과할 수 없습니다.");
        }

        UserPoint updatedPoint = userPointTable.insertOrUpdate(id, newPoint);
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, updatedPoint.updateMillis());
        return updatedPoint;
    }

}
