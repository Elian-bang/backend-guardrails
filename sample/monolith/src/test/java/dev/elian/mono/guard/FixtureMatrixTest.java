package dev.elian.mono.guard;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import dev.elian.guard.rules.Fixture;

import dev.elian.guard.rules.LayerRules;
import dev.elian.guard.rules.TransactionRules;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * 규칙별 <b>탐지율과 오탐률</b>을 측정한다.
 *
 * <p>탐지율만으로는 부족하다. 모든 걸 잡는 규칙은 탐지율 100% 지만 팀이 바로 꺼버린다.
 * 그래서 위반처럼 보이지만 정상인 <b>경계 사례</b>를 따로 두고 오탐을 센다.
 */
class FixtureMatrixTest {

    private static final Map<String, ArchRule> RULES = new LinkedHashMap<>();

    static {
        RULES.put("R1",  TransactionRules.R1_컨트롤러에_트랜잭션_금지);
        RULES.put("R1b", TransactionRules.R1b_컨트롤러_메서드에_트랜잭션_금지);
        RULES.put("R3",  TransactionRules.R3_트랜잭션_안_외부호출_금지);
        RULES.put("R4",  TransactionRules.R4_트랜잭션은_public_에만);
        RULES.put("R7",  TransactionRules.R7_트랜잭션_안_블로킹_대기_금지);
    }

    private record Row(String rule, int badTotal, int detected,
                       int goodTotal, int falsePositive,
                       int gapTotal, int gapClosed) {
        double 탐지율() { return badTotal == 0 ? 0 : detected * 100.0 / badTotal; }
        double 오탐률() { return goodTotal == 0 ? 0 : falsePositive * 100.0 / goodTotal; }

        /**
         * 알려진 위반 전체(bad + gap) 중 실제로 잡는 비율.
         *
         * <p>탐지율은 우리가 고른 fixture 기준이라 과대평가된다.
         * 못 잡는 것까지 분모에 넣어야 정직한 숫자가 된다.
         */
        double 커버리지() {
            int all = badTotal + gapTotal;
            return all == 0 ? 0 : (detected + gapClosed) * 100.0 / all;
        }

        /**
         * 차단으로 올릴지 판정한다.
         *
         * <p>오탐이 하나라도 있으면 차단하지 않는다. 멀쩡한 코드를 막는 규칙은
         * 팀이 가드레일 전체를 꺼버리는 원인이 된다.
         *
         * <p>알려진 한계(미탐)가 있어도 차단은 한다 — 못 잡는 게 있다는 것과
         * 잡은 것이 진짜 위반이라는 것은 별개다.
         */
        String 권고() {
            if (rule.endsWith("*")) return "미구현";
            if (badTotal == 0)      return "fixture 없음";
            if (falsePositive > 0)  return "WARN (오탐)";
            if (detected < badTotal) return "WARN (미탐지)";
            return "BLOCK";
        }
    }

    @Test
    void 규칙별_탐지율과_오탐률() throws Exception {
        List<Class<?>> bad  = fixturesOf(Fixture.Expect.VIOLATION);
        List<Class<?>> good = fixturesOf(Fixture.Expect.CLEAN);
        List<Class<?>> gaps = fixturesOf(Fixture.Expect.KNOWN_GAP);

        List<Row> rows = new ArrayList<>();
        for (var e : RULES.entrySet()) {
            String id = e.getKey();
            ArchRule rule = e.getValue();

            List<Class<?>> targets  = bad.stream().filter(c -> tagOf(c).rule().equals(id)).toList();
            List<Class<?>> gapsOfId = gaps.stream().filter(c -> tagOf(c).rule().equals(id)).toList();
            int detected  = (int) targets.stream().filter(c -> fails(rule, c)).count();
            int fp        = (int) good.stream().filter(c -> fails(rule, c)).count();
            int gapClosed = (int) gapsOfId.stream().filter(c -> fails(rule, c)).count();

            rows.add(new Row(id, targets.size(), detected, good.size(), fp, gapsOfId.size(), gapClosed));
        }
        // 계층 규칙은 패키지 단위라 따로 잰다
        rows.add(layerRow(bad, good));

        // fixture 는 있는데 규칙이 아직 없는 것. 안 적으면 표가 "다 잡는다"로 보인다
        Set<String> implemented = new LinkedHashSet<>(RULES.keySet());
        implemented.add("R6");
        Stream.concat(bad.stream(), gaps.stream())
                .map(c -> tagOf(c).rule())
                .distinct()
                .filter(id -> !implemented.contains(id))
                .forEach(id -> {
                    int b = (int) bad.stream().filter(c -> tagOf(c).rule().equals(id)).count();
                    int g = (int) gaps.stream().filter(c -> tagOf(c).rule().equals(id)).count();
                    rows.add(new Row(id + "*", b, 0, good.size(), 0, g, 0));
                });

        print(rows);

        rows.stream().filter(r -> r.gapClosed() > 0).forEach(r ->
                System.out.printf("  ↑ %s — 알려진 한계 %d건이 이제 잡힌다. VIOLATION 으로 승격할 것%n",
                        r.rule(), r.gapClosed()));

        rows.forEach(r -> {
            if (r.detected() < r.badTotal())
                throw new AssertionError("%s — 위반 %d건 중 %d건만 잡았다. 규칙이 죽었거나 fixture 가 규칙을 자극하지 못한다"
                        .formatted(r.rule(), r.badTotal(), r.detected()));
        });
    }

    private Row layerRow(List<Class<?>> bad, List<Class<?>> good) {
        boolean caught = failsOnPackage(LayerRules.R6_계층("dev.elian.fixtures.violations"), "dev.elian.fixtures.violations");
        boolean fp = failsOnPackage(LayerRules.R6_계층("dev.elian.fixtures.boundary"), "dev.elian.fixtures.boundary");
        int badCount = (int) bad.stream().filter(c -> tagOf(c).rule().equals("R6")).count();
        int goodCount = (int) good.stream().filter(c -> tagOf(c).rule().equals("R6")).count();
        return new Row("R6", badCount, caught ? badCount : 0, goodCount, fp ? goodCount : 0, 0, 0);
    }

    private static boolean fails(ArchRule rule, Class<?> c) {
        try {
            rule.check(new ClassFileImporter().importClasses(c));
            return false;
        } catch (AssertionError e) {
            return true;
        }
    }

    private static boolean failsOnPackage(ArchRule rule, String pkg) {
        try {
            rule.check(new ClassFileImporter().importPackages(pkg));
            return false;
        } catch (AssertionError e) {
            return true;
        }
    }

    private static Fixture tagOf(Class<?> c) { return c.getAnnotation(Fixture.class); }

    /** 테스트 클래스패스에서 {@code @Fixture} 가 붙은 클래스를 모은다. */
    private static List<Class<?>> fixturesOf(Fixture.Expect expect) throws IOException, URISyntaxException {
        List<Class<?>> out = new ArrayList<>();
        for (String pkg : List.of("dev/elian/fixtures/violations", "dev/elian/fixtures/boundary", "dev/elian/fixtures/gaps")) {
            Path root = Path.of(FixtureMatrixTest.class.getResource("/").toURI()).resolve(pkg);
            if (!Files.exists(root)) continue;
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path p : paths.filter(x -> x.toString().endsWith(".class")).toList()) {
                    String fqn = Path.of(FixtureMatrixTest.class.getResource("/").toURI())
                            .relativize(p).toString()
                            .replace(".class", "")
                            .replace(java.io.File.separatorChar, '.')
                            .replace('/', '.');
                    try {
                        Class<?> c = Class.forName(fqn);
                        Fixture f = c.getAnnotation(Fixture.class);
                        if (f != null && f.expect() == expect) out.add(c);
                    } catch (Throwable ignored) { }
                }
            }
        }
        return out;
    }

    private static void print(List<Row> rows) {
        System.out.println();
        System.out.println("| 규칙 | bad | 탐지 | 탐지율 | good | 오탐 | 오탐률 | 미탐 | 커버리지 | 권고 |");
        System.out.println("|---|---|---|---|---|---|---|---|---|---|");
        rows.forEach(r -> System.out.printf("| %-4s | %d | %d | %.0f%% | %d | %d | %.0f%% | %d | %.0f%% | %s |%n",
                r.rule(), r.badTotal(), r.detected(), r.탐지율(),
                r.goodTotal(), r.falsePositive(), r.오탐률(),
                r.gapTotal() - r.gapClosed(), r.커버리지(), r.권고()));
        System.out.println();
        System.out.println("  미탐 = 위반인 줄 알면서도 현재 규칙이 못 잡는 것 (KNOWN_GAP)");
        System.out.println("  * 표시 = fixture 는 있으나 규칙이 아직 구현되지 않음");
        System.out.println("  커버리지 = (탐지 + 닫힌 한계) / (bad + gap) — 우리가 고른 fixture 기준인 탐지율보다 정직하다");
        System.out.println();
    }
}
