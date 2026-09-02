package dev.elian.fixtures.violations;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** R4 위반 — 프록시를 안 거쳐서 애노테이션이 조용히 무시된다. */
@Fixture(rule = "R4", expect = Fixture.Expect.VIOLATION)
@Service
public class R4_private_에_트랜잭션 {

    public void process() {
        updateInternal();
    }

    @Transactional
    private void updateInternal() {
        // 트랜잭션이 열리지 않는다
    }
}
