package dev.elian.guard.rules;

import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/** 계층 규칙. 계층이 무너지면 트랜잭션 경계 규칙(R1·R3)도 같이 무너진다. */
public final class LayerRules {

    private LayerRules() {}

    /**
     * R6 — Repository 는 Service 에서만 부른다.
     *
     * <p>Controller 가 Repository 를 직접 부르면 트랜잭션 경계와 도메인 규칙이 우회된다.
     */
    public static ArchRule R6_계층(String basePackage) {
        return layeredArchitecture().consideringAllDependencies()
                .layer("Controller").definedBy(basePackage + "..controller..")
                .layer("Service").definedBy(basePackage + "..service..")
                .layer("Repository").definedBy(basePackage + "..repository..")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .because("Controller 가 Repository 를 직접 부르면 트랜잭션 경계가 우회된다")
                .allowEmptyShould(true);
    }
}
