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
    @DisplayName("특정 유저의 포인트를 조회한다.")
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
    @DisplayName("특정 유저의 포인트 내역을 조회한다.")
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

}
