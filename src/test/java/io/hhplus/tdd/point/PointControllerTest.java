package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
    @DisplayName("특정 유저의 포인트 정보 조회가 성공하고 200 응답을 반환하는지 검증")
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

    @Test
    @DisplayName("충전 금액이 0원 이하일 때 예외가 발생해 400 응답을 반환하는지 검증")
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



}