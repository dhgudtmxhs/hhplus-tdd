package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointTest {

    private static final long USER_ID = 1L;

    @Test
    @DisplayName("충전 금액이 0원 이하일 때 예외가 발생한다.")
    void charge_FailsWhenAmountIsZeroOrNegative() {
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.charge(-100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원 이하일 수 없습니다.");
    }

    @Test
    @DisplayName("충전 시 최대 잔고를 초과하면 예외가 발생한다.")
    void charge_FailsWhenExceedingMaxPoint() {
        UserPoint userPoint = new UserPoint(USER_ID, 999_999L, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.charge(100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 충전은 최대 잔고를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("충전 시 예외상황을 통과하면 특정 유저의 포인트를 충전한다.")
    void charge_Success() {
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());
        UserPoint updatedPoint = userPoint.charge(200L);

        assertThat(updatedPoint.point()).isEqualTo(800L);
    }

    @Test
    @DisplayName("사용 금액이 0원 이하일 때 예외가 발생한다.")
    void use_FailsWhenAmountIsZeroOrNegative() {
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.use(-50L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 금액은 0원 이하일 수 없습니다.");
    }

    @Test
    @DisplayName("사용 시 잔고가 부족할 경우 예외가 발생한다.")
    void use_FailsWhenAmountExceedsBalance() {
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        assertThatThrownBy(() -> userPoint.use(5000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트가 보유한 포인트보다 많습니다.");
    }

    @Test
    @DisplayName("포인트 사용 예외 상황을 통과했을 때 특정 유저의 포인트를 사용한다.")
    void use_Success() {
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());
        UserPoint updatedPoint = userPoint.use(500L);

        assertThat(updatedPoint.point()).isEqualTo(500L);
    }

}