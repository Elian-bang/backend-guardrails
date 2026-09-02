package dev.elian.mono.violations;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** R7 위반 — 락을 쥔 채로 잠든다. */
@Fixture(rule = "R7", expect = Fixture.Expect.VIOLATION)
@Service
public class R7_트랜잭션_안_블로킹 {

    @Transactional
    public void retryWithBackoff() throws InterruptedException {
        Thread.sleep(1000);
    }
}
