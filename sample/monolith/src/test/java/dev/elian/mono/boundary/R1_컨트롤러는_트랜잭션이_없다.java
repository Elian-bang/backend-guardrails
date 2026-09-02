package dev.elian.mono.boundary;

import dev.elian.guard.rules.Fixture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 여러 서비스를 부르지만 트랜잭션은 없다. 정상. */
@Fixture(rule = "R1", expect = Fixture.Expect.CLEAN,
         note = "Controller 가 서비스를 여러 개 호출한다. 트랜잭션이 없으므로 정상")
@RestController
public class R1_컨트롤러는_트랜잭션이_없다 {

    private final R1_이름만_컨트롤러 a;
    private final R3_트랜잭션_밖_외부호출 b;

    public R1_컨트롤러는_트랜잭션이_없다(R1_이름만_컨트롤러 a, R3_트랜잭션_밖_외부호출 b) {
        this.a = a;
        this.b = b;
    }

    @GetMapping("/ok/r1")
    public String handle() {
        a.doWork();
        b.callOutside();
        return "ok";
    }
}
