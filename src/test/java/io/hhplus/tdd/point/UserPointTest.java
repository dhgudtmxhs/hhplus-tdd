package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPointTest {

    private static final long USER_ID = 1L;

    @Test
    @DisplayName("유저가 포인트 충전 금액이 0원 이하일 때 충전하면 IllegalArgumentException 예외가 발생한다.")
    void charge_FailsWhenAmountIsZeroOrNegative() {
        // given
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        // when & then
        assertThatThrownBy(() -> userPoint.charge(-100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원 이하일 수 없습니다.");
    }

    @Test
    @DisplayName("유저가 포인트 충전 시 최대 잔고를 초과하면 IllegalArgumentException 예외가 발생한다.")
    void charge_FailsWhenExceedingMaxPoint() {
        // given
        UserPoint userPoint = new UserPoint(USER_ID, 999_999L, System.currentTimeMillis());

        // when & then
        assertThatThrownBy(() -> userPoint.charge(100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 충전은 최대 잔고를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("유저가 포인트를 충전하면 잔액이 올바르게 증가한다.")
    void charge_Success() {
        // given
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        // when
        UserPoint updatedPoint = userPoint.charge(200L);

        // then
        assertThat(updatedPoint.point()).isEqualTo(1200L);
    }

    @Test
    @DisplayName("유저가 포인트 사용 금액이 0원 이하일 때 사용하면 IllegalArgumentException 예외가 발생한다.")
    void use_FailsWhenAmountIsZeroOrNegative() {
        // given
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        // when & then
        assertThatThrownBy(() -> userPoint.use(-50L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 금액은 0원 이하일 수 없습니다.");
    }

    @Test
    @DisplayName("유저가 포인트 사용 시 잔고가 부족할 경우 IllegalArgumentException 예외가 발생한다.")
    void use_FailsWhenAmountExceedsBalance() {
        // given
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        // when & then
        assertThatThrownBy(() -> userPoint.use(5000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용할 포인트가 보유한 포인트보다 많습니다.");
    }

    @Test
    @DisplayName("유저가 포인트를 사용하면 잔액이 올바르게 감소한다.")
    void use_Success() {
        // given
        UserPoint userPoint = new UserPoint(USER_ID, 1000L, System.currentTimeMillis());

        // when
        UserPoint updatedPoint = userPoint.use(500L);

        // then
        assertThat(updatedPoint.point()).isEqualTo(500L);
    }

}