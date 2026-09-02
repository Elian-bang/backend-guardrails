package dev.elian.mono.boundary.controller;

import dev.elian.guard.rules.Fixture;
import dev.elian.mono.boundary.service.OkFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller → Service. 정상 방향이고 Repository 를 직접 부르지 않는다. */
@Fixture(rule = "R6", expect = Fixture.Expect.CLEAN,
         note = "Controller → Service 만 부른다. Repository 직접 호출 없음")
@RestController
public class OkController {

    private final OkFacade facade;

    public OkController(OkFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/ok/r6")
    public long count() {
        return facade.total();
    }
}
