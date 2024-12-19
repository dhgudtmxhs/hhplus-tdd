package io.hhplus.tdd.point;

import io.hhplus.tdd.point.domain.PointHistory;
import io.hhplus.tdd.point.domain.PointService;
import io.hhplus.tdd.point.domain.TransactionType;
import io.hhplus.tdd.point.domain.UserPoint;
import io.hhplus.tdd.point.interfaces.PointController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PointService pointService;

    private static final long USER_ID = 1L;

    @Test
    @DisplayName("특정 유저의 포인트 정보 조회가 성공하고 200 응답을 반환한다.")
    void getUserPoint_Success() throws Exception {
        // Given
        UserPoint mockUserPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        when(pointService.getUserPoint(USER_ID)).thenReturn(mockUserPoint);

        // When & Then
        mockMvc.perform(get("/point/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 응답 검증
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.point").value(1000L));
    }

    // 추가 테스트: 특정 유저의 포인트 내역 조회 성공
    @Test
    @DisplayName("특정 유저의 포인트 내역 조회가 성공하고 200 응답을 반환한다.")
    void getUserPointHistories_Success() throws Exception {
        // Given
        PointHistory history1 = new PointHistory(1L, USER_ID, 500L, TransactionType.CHARGE, 1000L);
        PointHistory history2 = new PointHistory(2L, USER_ID, -200L, TransactionType.USE, 800L);
        List<PointHistory> mockHistories = List.of(history1, history2);

        when(pointService.getUserPointHistories(USER_ID)).thenReturn(mockHistories);

        // When & Then
        mockMvc.perform(get("/point/{id}/histories", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 응답 검증
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(500L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].amount").value(-200L));
    }


    @Test
    @DisplayName("충전 금액이 0원 이하일 때 예외가 발생해 400 응답을 반환한다.")
    void chargeUserPoint_FailsWhenAmountIsZeroOrNegative() throws Exception {
        // Given
        long chargeAmount = -100L;
        when(pointService.chargeUserPoint(USER_ID, chargeAmount))
                .thenThrow(new IllegalArgumentException("충전 금액은 0원 이하일 수 없습니다."));

        // When & Then
        mockMvc.perform(patch("/point/{id}/charge", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isBadRequest()) // 400 응답 검증
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("충전 금액은 0원 이하일 수 없습니다."));
    }

    // 추가 테스트: 포인트 사용 시 잔고 부족 예외 발생
    @Test
    @DisplayName("포인트 사용 시 잔고가 부족하면 예외가 발생해 400 응답을 반환한다.")
    void useUserPoint_FailsWhenBalanceNotEnough() throws Exception {
        // Given
        long useAmount = 1_000L;
        when(pointService.useUserPoint(USER_ID, useAmount))
                .thenThrow(new IllegalArgumentException("사용할 포인트가 보유한 포인트보다 많습니다."));

        // When & Then
        mockMvc.perform(patch("/point/{id}/use", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isBadRequest()) // 400 응답 검증
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("사용할 포인트가 보유한 포인트보다 많습니다."));
    }



}