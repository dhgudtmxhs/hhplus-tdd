package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    private static final long MAX_POINT = 1_000_000L; // 최대 잔고 정책

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    // 포인트 충전 정책
    public UserPoint charge(long amount) {
        if(amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원 이하일 수 없습니다.");
        }

        long newPoint = this.point + amount;

        if(newPoint > MAX_POINT) {
            throw new IllegalArgumentException("포인트 충전은 최대 잔고를 초과할 수 없습니다.");
        }

        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }

    // 포인트 사용 정책
    public UserPoint use(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0원 이하일 수 없습니다.");
        }

        long newPoint = this.point - amount;

        if (newPoint < 0) {
            throw new IllegalArgumentException("사용할 포인트가 보유한 포인트보다 많습니다.");
        }

        return new UserPoint(this.id, newPoint, System.currentTimeMillis());
    }



}
