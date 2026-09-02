package dev.elian.mono.violations;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * R1 위반 — <b>클래스 레벨</b> {@code @Transactional}.
 *
 * <p>메서드 레벨(R1b)과 별개다. 처음엔 메서드 fixture 만 있어서 R1 이 아무것도 못 잡았고,
 * 메타 테스트가 그걸 잡아냈다.
 */
@RestController
@Transactional
public class R1_컨트롤러_클래스에_트랜잭션 {

    @GetMapping("/bad/r1-class")
    public String list() {
        return "ok";
    }
}
