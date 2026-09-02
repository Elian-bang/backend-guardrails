package dev.elian.mono.guard;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import dev.elian.guard.rules.Guardrails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * 앱 본체에 규칙을 건다. <b>전부 통과해야 한다.</b>
 *
 * <p>여기서 실패하면 둘 중 하나다 — 앱이 규칙을 어겼거나, <b>규칙이 멀쩡한 코드를 잡는 오탐</b>이거나.
 * 본체가 곧 {@code good} fixture 라서, 오탐이 있으면 그 자리에서 드러난다.
 */
class GuardrailTest {

    private static final String BASE = "dev.elian.mono";

    /** 테스트 소스는 뺀다. 위반 fixture 가 여기 섞이면 안 된다. */
    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);

    static Stream<Guardrails.Rule> blockingRules() {
        return Guardrails.blocking(BASE).values().stream();
    }

    @DisplayName("앱 본체는 차단 규칙을 전부 통과한다")
    @ParameterizedTest(name = "{0}")
    @MethodSource("blockingRules")
    void 본체는_차단_규칙을_통과한다(Guardrails.Rule rule) {
        rule.rule().check(PRODUCTION);
    }
}
