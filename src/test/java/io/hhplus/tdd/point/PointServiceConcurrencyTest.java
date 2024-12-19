package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Test
    @DisplayName("여러 사용자가 동시에 요청할 때 동일 사용자에 대한 충전/사용 작업은 순차적으로 처리된다.")
    void concurrentChargeAndUseMultiUsersTest() throws InterruptedException {
        // Given
        final Long userId1 = 1L;
        final Long userId2 = 2L;
        final Long userId3 = 3L;
        final int threadCount = 30; // 작업 수
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 쓰레드 풀 설정
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount); // 카운트 설정

        // When: 각 10개의 충전 및 사용
        for (int i = 0; i < threadCount; i++) {
            final Long userId;
            if (i % 3 == 0) {
                userId = userId1;
            } else if (i % 3 == 1) {
                userId = userId2;
            } else {
                userId = userId3;
            }

            executorService.submit(() -> {
                try {
                    pointService.chargeUserPoint(userId, 1000L);
                    pointService.useUserPoint(userId, 400L);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();

        // Then
        UserPoint finalUserPoint1 = pointService.getUserPoint(userId1);
        assertThat(finalUserPoint1.point()).isEqualTo(6000L);

        UserPoint finalUserPoint2 = pointService.getUserPoint(userId2);
        assertThat(finalUserPoint2.point()).isEqualTo(6000L);

        UserPoint finalUserPoint3 = pointService.getUserPoint(userId3);
        assertThat(finalUserPoint3.point()).isEqualTo(6000L);
    }

}