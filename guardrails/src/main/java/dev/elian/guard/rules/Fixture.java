package dev.elian.guard.rules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * fixture 가 어느 규칙을 겨냥하는지, 그리고 잡혀야 하는지 표시한다.
 *
 * <p>이게 있어야 <b>규칙별</b> 탐지율과 오탐률을 낼 수 있다.
 * 전체 합산 숫자로는 어느 규칙이 약한지 안 보인다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Fixture {

    /** 겨냥하는 규칙 id. 예: "R3" */
    String rule();

    Expect expect();

    /** 왜 이 fixture 가 헷갈리는지 (CLEAN 인 경우 특히 중요) */
    String note() default "";

    enum Expect {
        /** 규칙이 <b>반드시 잡아야</b> 한다 */
        VIOLATION,
        /** 위반처럼 보이지만 정상이다. 규칙이 잡으면 <b>오탐</b> */
        CLEAN,
        /**
         * <b>위반인데 현재 규칙이 못 잡는다.</b> 알려진 한계다.
         *
         * <p>못 잡는 것을 안 적으면 탐지율이 과대평가된다.
         * "우리가 만든 fixture 는 다 잡는다"는 표는 그 fixture 를 우리가 골랐다는 사실을 숨긴다.
         *
         * <p>규칙이 개선돼 이걸 잡게 되면 좋은 소식이다 — 빌드를 깨지 않고 "닫힘"으로 보고한다.
         * 그때 { #VIOLATION} 으로 승격한다.
         */
        KNOWN_GAP
    }
}
