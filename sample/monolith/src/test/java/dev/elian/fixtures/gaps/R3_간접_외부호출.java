package dev.elian.fixtures.gaps;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * <b>진짜 위반인데 못 잡는다.</b>
 *
 * <p>트랜잭션 메서드가 외부 호출을 <b>직접</b> 하지 않고 헬퍼를 거친다.
 * 현재 규칙은 호출을 <b>한 단계만</b> 본다. 실행 시점에는 락을 쥔 채 채널 응답을 기다리는데도
 * 정적 분석은 통과시킨다.
 *
 * <p>닫으려면 호출 그래프를 재귀로 따라가야 하고, 그러면 분석 시간과 오탐이 같이 늘어난다.
 */
@Fixture(rule = "R3", expect = Fixture.Expect.KNOWN_GAP,
         note = "트랜잭션 안에서 헬퍼를 거쳐 외부 호출. 규칙이 한 단계만 봐서 못 잡는다")
@Service
public class R3_간접_외부호출 {

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void confirmAndNotify(String recipient) {
        callChannel(recipient);        // 한 단계 건너뛴다
    }

    private void callChannel(String recipient) {
        restTemplate.getForObject("https://channel.example/send?to=" + recipient, String.class);
    }
}
