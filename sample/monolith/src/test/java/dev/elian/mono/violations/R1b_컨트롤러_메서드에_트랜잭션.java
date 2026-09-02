package dev.elian.mono.violations;

import dev.elian.guard.rules.Fixture;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** R1b 위반 — Controller <b>메서드</b>에 트랜잭션. */
@Fixture(rule = "R1b", expect = Fixture.Expect.VIOLATION)
@RestController
public class R1b_컨트롤러_메서드에_트랜잭션 {

    @Transactional
    @PostMapping("/bad/r1")
    public String book() {
        return "ok";
    }
}
