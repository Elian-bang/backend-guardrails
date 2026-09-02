package dev.elian.guard.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 앱이 쓰는 진입점.
 *
 * <p>규칙을 <b>차단(BLOCK)</b>과 <b>경고(WARN)</b>로 나눈다.
 * 오탐률을 재기 전인 규칙을 빌드 실패로 걸면 팀이 가드레일 전체를 불신한다.
 * 오탐 0 이 확인된 규칙만 차단으로 올린다.
 */
public final class Guardrails {

    private Guardrails() {}

    public enum Level { BLOCK, WARN }

    public record Rule(String id, Level level, ArchRule rule) {}

    /** 오탐 0 이 확인된 규칙. 위반하면 빌드가 깨진다. */
    public static Map<String, Rule> blocking(String basePackage) {
        Map<String, Rule> m = new LinkedHashMap<>();
        put(m, "R1",  Level.BLOCK, TransactionRules.R1_컨트롤러에_트랜잭션_금지);
        put(m, "R1b", Level.BLOCK, TransactionRules.R1b_컨트롤러_메서드에_트랜잭션_금지);
        put(m, "R3",  Level.BLOCK, TransactionRules.R3_트랜잭션_안_외부호출_금지);
        put(m, "R4",  Level.BLOCK, TransactionRules.R4_트랜잭션은_public_에만);
        put(m, "R6",  Level.BLOCK, LayerRules.R6_계층(basePackage));
        put(m, "R7",  Level.BLOCK, TransactionRules.R7_트랜잭션_안_블로킹_대기_금지);
        return m;
    }

    private static void put(Map<String, Rule> m, String id, Level lv, ArchRule r) {
        m.put(id, new Rule(id, lv, r));
    }

    /** 전체 검사. 위반이 있으면 예외로 터진다. */
    public static void checkAll(JavaClasses classes, String basePackage) {
        blocking(basePackage).values().forEach(r -> r.rule().check(classes));
    }
}
