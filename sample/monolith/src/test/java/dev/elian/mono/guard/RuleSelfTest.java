package dev.elian.mono.guard;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import dev.elian.guard.rules.LayerRules;
import dev.elian.guard.rules.TransactionRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * <b>규칙을 테스트하는 테스트.</b>
 *
 * <p>ArchUnit 규칙은 통과만 하면 죽어 있어도 모른다. 조건을 빈 값으로 바꿔놔도
 * 앱 검사는 초록불이다. 그래서 <b>위반 fixture 에서 반드시 실패하는지</b>를 따로 확인한다.
 *
 * <p>규칙 하나당 둘 —
 * <ul>
 *   <li>위반에서 실패한다 ({@link RuleSelfTest})</li>
 *   <li>정상을 통과한다 ({@link GuardrailTest} — 앱 본체가 곧 정상 fixture)</li>
 * </ul>
 */
class RuleSelfTest {

    /** 위반 fixture 만 읽는다. */
    private static final JavaClasses VIOLATIONS =
            new ClassFileImporter().importPackages("dev.elian.mono.violations");

    private void 반드시_잡아야_한다(ArchRule rule, String 무엇을) {
        assertThatThrownBy(() -> rule.check(VIOLATIONS))
                .as("규칙이 %s 를 잡지 못했다 — 규칙이 죽었을 수 있다", 무엇을)
                .isInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("R1 — Controller 클래스의 @Transactional 을 잡는다")
    void R1() {
        반드시_잡아야_한다(TransactionRules.R1_컨트롤러에_트랜잭션_금지, "Controller 트랜잭션");
    }

    @Test
    @DisplayName("R1b — Controller 메서드의 @Transactional 을 잡는다")
    void R1b() {
        반드시_잡아야_한다(TransactionRules.R1b_컨트롤러_메서드에_트랜잭션_금지, "Controller 메서드 트랜잭션");
    }

    @Test
    @DisplayName("R3 — 트랜잭션 안 외부 호출을 잡는다")
    void R3() {
        반드시_잡아야_한다(TransactionRules.R3_트랜잭션_안_외부호출_금지, "트랜잭션 안 RestTemplate 호출");
    }

    @Test
    @DisplayName("R4 — private 메서드의 @Transactional 을 잡는다")
    void R4() {
        반드시_잡아야_한다(TransactionRules.R4_트랜잭션은_public_에만, "private @Transactional");
    }

    @Test
    @DisplayName("R6 — Controller 가 Repository 를 직접 부르는 것을 잡는다")
    void R6() {
        반드시_잡아야_한다(LayerRules.R6_계층("dev.elian.mono.violations"), "계층 위반");
    }

    @Test
    @DisplayName("R7 — 트랜잭션 안 블로킹 대기를 잡는다")
    void R7() {
        반드시_잡아야_한다(TransactionRules.R7_트랜잭션_안_블로킹_대기_금지, "트랜잭션 안 Thread.sleep");
    }
}
