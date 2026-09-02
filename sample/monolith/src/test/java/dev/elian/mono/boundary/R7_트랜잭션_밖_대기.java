package dev.elian.mono.boundary;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 재시도 백오프를 <b>트랜잭션 밖에서</b> 한다. 실제 발송기가 쓰는 모양이다.
 */
@Fixture(rule = "R7", expect = Fixture.Expect.CLEAN,
         note = "Thread.sleep 이 있지만 트랜잭션 밖이다. 락을 쥔 채 잠들지 않는다")
@Service
public class R7_트랜잭션_밖_대기 {

    public void retryLoop() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread.sleep(100);      // 트랜잭션 밖
            markAttempt();          // 트랜잭션은 여기서만 열린다
        }
    }

    @Transactional
    public void markAttempt() { }
}
