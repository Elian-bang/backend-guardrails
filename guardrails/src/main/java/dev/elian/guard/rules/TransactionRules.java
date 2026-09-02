package dev.elian.guard.rules;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

/**
 * 트랜잭션 규칙. 문서는 docs/rules/트랜잭션-규칙-10.md.
 *
 * <p>여기 있는 규칙은 <b>앱이 고칠 수 없다.</b> 앱은 고정 버전을 의존해서 가져다 쓴다.
 * 규칙 변경은 이 모듈의 별도 PR 을 거친다.
 */
public final class TransactionRules {

    private TransactionRules() {}

    private static final String TX = "org.springframework.transaction.annotation.Transactional";

    /** 외부로 나가는 호출로 취급하는 타입들. 트랜잭션 안에서 부르면 안 된다. */
    private static final String[] OUTBOUND = {
            "org.springframework.web.client.RestClient",
            "org.springframework.web.client.RestTemplate",
            "org.springframework.web.reactive.function.client.WebClient",
            "org.springframework.amqp.rabbit.core.RabbitTemplate",
            "org.springframework.kafka.core.KafkaTemplate",
            "java.net.http.HttpClient",
    };

    /** 트랜잭션 안에서 부르면 안 되는 블로킹 대기. */
    private static final String[] BLOCKING = {
            "java.lang.Thread.sleep",
            "java.util.concurrent.Future.get",
            "java.util.concurrent.CountDownLatch.await",
            "java.util.concurrent.TimeUnit.sleep",
    };

    // ─────────────────────────── R1 ───────────────────────────

    /** Controller 트랜잭션은 요청 전체를 감싼다. 경계는 유스케이스 단위여야 한다. */
    public static final ArchRule R1_컨트롤러에_트랜잭션_금지 =
            noClasses()
                    .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .or().areAnnotatedWith("org.springframework.stereotype.Controller")
                    .should().beAnnotatedWith(TX)
                    .because("Controller 트랜잭션은 직렬화·외부 호출까지 감싸 커넥션을 오래 문다")
                    .allowEmptyShould(true);

    public static final ArchRule R1b_컨트롤러_메서드에_트랜잭션_금지 =
            noMethods()
                    .that().areDeclaredInClassesThat()
                    .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .should().beAnnotatedWith(TX)
                    .because("Controller 트랜잭션은 직렬화·외부 호출까지 감싼다")
                    .allowEmptyShould(true);

    // ─────────────────────────── R4 ───────────────────────────

    /** 프록시는 바깥에서 들어오는 호출만 가로챈다. private 에 붙이면 조용히 무시된다. */
    public static final ArchRule R4_트랜잭션은_public_에만 =
            methods()
                    .that().areAnnotatedWith(TX)
                    .should().bePublic()
                    .because("Spring AOP 프록시는 public 호출만 가로챈다 — private 에 붙으면 애노테이션이 무시된다")
                    .allowEmptyShould(true);

    // ─────────────────────────── R3 ───────────────────────────

    /** 응답 시간이 통제 밖인 호출을 트랜잭션 안에 두면 락과 커넥션을 남이 정한 시간만큼 붙잡는다. */
    public static final ArchRule R3_트랜잭션_안_외부호출_금지 =
            methods()
                    .that().areAnnotatedWith(TX)
                    .should(notCall(OUTBOUND, "외부 시스템 호출"))
                    .because("외부 호출은 응답 시간이 통제 밖이라, 그동안 락과 커넥션을 붙잡는다")
                    .allowEmptyShould(true);

    // ─────────────────────────── R7 ───────────────────────────

    /** 트랜잭션 안에서 잠들면 락을 쥔 채로 잠든다. R3 의 일반화. */
    public static final ArchRule R7_트랜잭션_안_블로킹_대기_금지 =
            methods()
                    .that().areAnnotatedWith(TX)
                    .should(notCall(BLOCKING, "블로킹 대기"))
                    .because("트랜잭션 안에서 대기하면 락을 쥔 채로 잠든다")
                    .allowEmptyShould(true);

    // ─────────────────────────── 공통 조건 ───────────────────────────

    /**
     * 메서드가 지정한 타입/메서드를 직접 호출하지 않아야 한다.
     *
     * <p>한 단계만 본다. 트랜잭션 메서드가 부른 다른 메서드 안의 외부 호출은 못 잡는다 —
     * 그 한계는 문서에 적어 두고, 잡히는 범위만 차단한다.
     */
    private static ArchCondition<JavaMethod> notCall(String[] targets, String label) {
        return new ArchCondition<>("should not call " + label) {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                for (JavaMethodCall call : method.getMethodCallsFromSelf()) {
                    String owner = call.getTargetOwner().getFullName();
                    String full = owner + "." + call.getName();
                    for (String t : targets) {
                        if (owner.equals(t) || full.equals(t) || isSubtypeNamed(call.getTargetOwner(), t)) {
                            events.add(SimpleConditionEvent.violated(method,
                                    "%s 이(가) 트랜잭션 안에서 %s 를 호출한다 (%s)"
                                            .formatted(method.getFullName(), full, label)));
                            return;
                        }
                    }
                }
            }
        };
    }

    private static boolean isSubtypeNamed(JavaClass type, String fqn) {
        return type.getAllRawSuperclasses().stream().anyMatch(c -> c.getFullName().equals(fqn))
                || type.getAllRawInterfaces().stream().anyMatch(c -> c.getFullName().equals(fqn));
    }
}
