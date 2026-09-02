package dev.elian.mono.boundary;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * private·protected 헬퍼가 있지만 <b>애노테이션이 없다.</b> 정상이다.
 * "private 메서드가 있는 트랜잭션 클래스"를 잡으면 오탐이다.
 */
@Fixture(rule = "R4", expect = Fixture.Expect.CLEAN,
         note = "private 헬퍼가 있지만 @Transactional 이 안 붙어 있다. 프록시와 무관하다")
@Service
public class R4_애노테이션_없는_private {

    @Transactional
    public void process() {
        validate();
        compute();
    }

    private void validate() { }

    protected void compute() { }
}
