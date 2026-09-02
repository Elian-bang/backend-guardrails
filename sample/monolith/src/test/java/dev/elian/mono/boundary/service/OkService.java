package dev.elian.mono.boundary.service;

import dev.elian.guard.rules.Fixture;
import dev.elian.mono.boundary.repository.OkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service 가 Repository 를 부른다. 정상 방향이다. */
@Fixture(rule = "R6", expect = Fixture.Expect.CLEAN,
         note = "Service → Repository 는 허용된 방향이다")
@Service
public class OkService {

    private final OkRepository repository;

    public OkService(OkRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}
