package dev.elian.mono.violations.controller;

import dev.elian.mono.violations.repository.BadRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** R6 위반 — Controller 가 Repository 를 직접 부른다. 트랜잭션 경계가 우회된다. */
@RestController
public class R6_컨트롤러가_리포지토리_직접호출 {

    private final BadRepository repository;

    public R6_컨트롤러가_리포지토리_직접호출(BadRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/bad/r6")
    public long count() {
        return repository.count();
    }
}
