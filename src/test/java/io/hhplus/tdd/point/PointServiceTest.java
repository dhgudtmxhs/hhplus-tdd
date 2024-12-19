package io.hhplus.tdd.point;


import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    UserPointTable userPointTable;

    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks
    PointService pointService;

    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("유저가 포인트를 조회하면 올바른 포인트가 반환된다.")
    void getUserPoint_returnCorrectUserPoint() {
        // Given
        UserPoint mockUserPoint = new UserPoint(USER_ID, 1000L, 500L);
        when(userPointTable.selectById(USER_ID)).thenReturn(mockUserPoint);

        // When
        UserPoint result = pointService.getUserPoint(USER_ID);

        // Then 반환된 userPoint가 예상대로인지 상태를 검증한다.
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.point()).isEqualTo(1000L);

        // selectById 메서드가 호출되었는지 행동을 검증한다.
        verify(userPointTable).selectById(USER_ID);
    }

    @Test
    @DisplayName("유저의 포인트 내역을 조회하면 올바른 내역이 반환된다.")
    void getUserPointHistories_returnCorrectHistories() {
        // Given
        PointHistory history1 = new PointHistory(1L, USER_ID, 500L, TransactionType.CHARGE, 1000L);
        PointHistory history2 = new PointHistory(2L, USER_ID, 200L, TransactionType.USE, 2000L);
        List<PointHistory> mockHistories = List.of(history1, history2);

        when(pointHistoryTable.selectAllByUserId(USER_ID)).thenReturn(mockHistories);

        // When
        List<PointHistory> result = pointService.getUserPointHistories(USER_ID);

        // Then 반환된 pointHistory가 예상대로인지 상태를 검증한다.
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(history1, history2);

        // selectAllByUserId 메서드가 호출되었는지 행동을 검증한다.
        verify(pointHistoryTable).selectAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("유저가 포인트 충전 금액이 0원 이하일 때 충전하면 IllegalArgumentException 예외가 발생한다.")
    void chargeUserPoint_FailsWhenAmountIsZeroOrNegative() {
        // Given
        long chargeAmount = -100L;
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, 500L, 0L));

        // When & Then
        assertThatThrownBy(() -> pointService.chargeUserPoint(USER_ID, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class) // 예외가 발생하는지 검증한다.
                .hasMessage("충전 금액은 0원 이하일 수 없습니다.");

        // insertOrUpdate와 insert 메서드가 호출되지 않았음을 검증한다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("유저가 포인트 충전 시 최대 잔고를 초과하면 IllegalArgumentException 예외가 발생한다.")
    void chargeUserPoint_FailsWhenExceedingMaxPoint() {
        // Given
        long currentPoint = 999_900L;
        long chargeAmount = 200L;
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, currentPoint, 0L));

        // When & Then
        assertThatThrownBy(() -> pointService.chargeUserPoint(USER_ID, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class) // 예외가 발생하는지 검증한다.
                .hasMessage("포인트 충전은 최대 잔고를 초과할 수 없습니다.");

        // insertOrUpdate와 insert 메서드가 호출되지 않았음을 검증한다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("유저가 포인트를 충전하면 잔액이 올바르게 증가한다.")
    void chargeUserPointSuccess() {
        // Given
        long currentPoint = 500_000L;
        long chargeAmount = 100_000L;
        long expectedNewPoint = currentPoint + chargeAmount;

        UserPoint currentUserPoint = new UserPoint(USER_ID, currentPoint, 0L);
        UserPoint updatedUserPoint = new UserPoint(USER_ID, expectedNewPoint, System.currentTimeMillis());

        when(userPointTable.selectById(USER_ID)).thenReturn(currentUserPoint);
        when(userPointTable.insertOrUpdate(USER_ID, expectedNewPoint)).thenReturn(updatedUserPoint);

        // When
        UserPoint result = pointService.chargeUserPoint(USER_ID, chargeAmount);

        // Then id와 point 상태를 검증한다.
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("updateMillis")
                .isEqualTo(updatedUserPoint);


        // insertOrUpdate와 insert 메서드가 호출되었음을 검증한다.
        verify(userPointTable).insertOrUpdate(USER_ID, expectedNewPoint);
        verify(pointHistoryTable).insert(eq(USER_ID), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("유저가 포인트 사용 금액이 0원 이하일 때 사용하면 IllegalArgumentException 예외가 발생한다.")
    void useUserPoint_FailsWhenAmountIsZeroOrNegative() {
        //Given
        long useAmount = 0L;
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, useAmount, 0L));

        //When & Then
        assertThatThrownBy(() -> pointService.useUserPoint(USER_ID, useAmount))
                .isInstanceOf(IllegalArgumentException.class) // 예외가 발생하는지 검증한다.
                .hasMessage("사용 금액은 0원 이하일 수 없습니다.");

        // 이후 메서드가 호출되지 않음을 검증한다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("유저가 포인트 사용 시 잔고가 부족할 경우 IllegalArgumentException 예외가 발생한다.")
    void useUserPoint_FailsWhenBalanceNotEnough() {
        // Given
        long currentPoint = 500L;
        long useAmount = 1_000L;
        when(userPointTable.selectById(USER_ID)).thenReturn(new UserPoint(USER_ID, currentPoint, 0L));

        // When & Then
        assertThatThrownBy(() -> pointService.useUserPoint(USER_ID, useAmount))
                .isInstanceOf(IllegalArgumentException.class) // 예외가 발생하는지 검증한다.
                .hasMessage("사용할 포인트가 보유한 포인트보다 많습니다.");

        // 이후 메서드가 호출되지 않음을 검증한다.
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(), anyLong());
    }

    @Test
    @DisplayName("유저가 포인트를 사용하면 잔액이 올바르게 감소한다.")
    void useUserPointSuccess() {
        // Given
        long currentPoint = 500L;
        long useAmount = 200L;
        long expectedNewPoint = currentPoint - useAmount;

        UserPoint currentUserPoint = new UserPoint(USER_ID, currentPoint, 0L);
        UserPoint updatedUserPoint = new UserPoint(USER_ID, expectedNewPoint, System.currentTimeMillis());

        when(userPointTable.selectById(USER_ID)).thenReturn(currentUserPoint);
        when(userPointTable.insertOrUpdate(USER_ID, expectedNewPoint)).thenReturn(updatedUserPoint);

        // When
        UserPoint result = pointService.useUserPoint(USER_ID, useAmount);

        // Then: id와 point 상태를 검증한다.
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("updateMillis")
                .isEqualTo(updatedUserPoint);

        // 이후 메서드가 호출됨을 검증한다.
        verify(userPointTable).insertOrUpdate(USER_ID, expectedNewPoint);
        verify(pointHistoryTable).insert(eq(USER_ID), eq(-useAmount), eq(TransactionType.USE), anyLong());
    }

    @Test
    @DisplayName("유저 ID가 0 이하일 때 포인트를 조회하면 IllegalArgumentException 예외가 발생한다.")
    void getUserPoint_FailsWhenIdIsInvalid() {
        // Given
        long invalidId = 0L;

        // When & Then
        assertThatThrownBy(() -> pointService.getUserPoint(invalidId))
                .isInstanceOf(IllegalArgumentException.class) // 예외를 검증한다.
                .hasMessage("유효하지 않은 유저 ID입니다. ID: " + invalidId);

        // 이후 메서드가 호출되지 않음을 검증한다.
        verify(userPointTable, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("유저 ID가 0 이하일 때 포인트 내역을 조회하면 IllegalArgumentException 예외가 발생한다.")
    void getUserPointHistories_FailsWhenIdIsInvalid() {
        // Given
        long invalidId = -1L;

        // When & Then
        assertThatThrownBy(() -> pointService.getUserPointHistories(invalidId))
                .isInstanceOf(IllegalArgumentException.class)// 예외를 검증한다.
                .hasMessage("유효하지 않은 유저 ID입니다. ID: " + invalidId);

        // 이후 메서드가 호출되지 않음을 검증한다.
        verify(pointHistoryTable, never()).selectAllByUserId(anyLong());
    }
}
