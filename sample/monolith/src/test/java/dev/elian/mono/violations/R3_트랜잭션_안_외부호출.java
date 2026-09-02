package dev.elian.mono.violations;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/** R3 위반 — 락을 쥔 채 채널사 응답을 기다린다. */
@Fixture(rule = "R3", expect = Fixture.Expect.VIOLATION)
@Service
public class R3_트랜잭션_안_외부호출 {

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void confirmAndNotify(String recipient) {
        restTemplate.getForObject("https://channel.example/send?to=" + recipient, String.class);
    }
}
