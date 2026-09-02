package dev.elian.fixtures.gaps;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>진짜 위반인데 못 잡는다.</b> R3 와 같은 이유 — 대기가 한 단계 건너에 있다.
 */
@Fixture(rule = "R7", expect = Fixture.Expect.KNOWN_GAP,
         note = "트랜잭션 안에서 헬퍼를 거쳐 sleep. 한 단계만 보는 한계")
@Service
public class R7_간접_블로킹 {

    @Transactional
    public void retryWithBackoff() {
        backoff();
    }

    private void backoff() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
