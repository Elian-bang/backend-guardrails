package dev.elian.mono.boundary;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 호출을 <b>트랜잭션 밖에서</b> 한다. 같은 클래스에 트랜잭션 메서드가 있어도 정상이다.
 * 클래스 단위로 잡으면 오탐이다.
 */
@Fixture(rule = "R3", expect = Fixture.Expect.CLEAN,
         note = "RestTemplate 필드를 갖고 있고 트랜잭션 메서드도 있지만, 외부 호출은 트랜잭션 밖에서만 한다")
@Service
public class R3_트랜잭션_밖_외부호출 {

    private final RestTemplate restTemplate = new RestTemplate();

    /** 트랜잭션이 없다. 외부 호출을 해도 된다 */
    public String callOutside() {
        return restTemplate.getForObject("https://example.com", String.class);
    }

    /** 트랜잭션이 있지만 외부 호출을 하지 않는다 */
    @Transactional
    public void saveOnly() {
        // DB 작업만
    }
}
