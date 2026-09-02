package dev.elian.mono.boundary;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 클래스 이름은 Controller 인데 <b>Spring 컨트롤러가 아니다.</b>
 * 이름만 보고 잡으면 오탐이다.
 */
@Fixture(rule = "R1", expect = Fixture.Expect.CLEAN,
         note = "이름에 Controller 가 들어가지만 @RestController 가 아니다. 트랜잭션은 정당하다")
@Service
public class R1_이름만_컨트롤러 {

    @Transactional
    public void doWork() {
        // 서비스다. 트랜잭션이 있어도 된다
    }
}
