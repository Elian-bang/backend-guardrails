package dev.elian.mono.gaps;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>진짜 위반인데 못 잡는다.</b>
 *
 * <p>{@code save()} 는 public 이라 R4 를 통과한다. 그런데 같은 클래스의 {@code process()} 가
 * {@code this.save()} 로 부르므로 <b>프록시를 거치지 않는다.</b> 트랜잭션이 열리지 않는데
 * 코드만 보면 열리는 것처럼 보인다.
 *
 * <p>R5 를 구현하면 잡을 수 있으나, 정상 케이스(트랜잭션 안에서 같은 클래스 헬퍼 호출)와
 * 구분이 어려워 오탐률을 먼저 재야 한다.
 */
@Fixture(rule = "R5", expect = Fixture.Expect.KNOWN_GAP,
         note = "self-invocation. public 이라 R4 를 통과하지만 프록시를 안 거쳐 트랜잭션이 없다")
@Service
public class R5_self_invocation {

    public void process() {
        save();                 // this.save() — 프록시 통과 안 함
    }

    @Transactional
    public void save() {
        // 트랜잭션이 열리지 않는다
    }
}
