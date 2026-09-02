package dev.elian.fixtures.boundary.service;

import dev.elian.guard.rules.Fixture;
import org.springframework.stereotype.Service;

/**
 * Service 가 다른 Service 를 부른다.
 *
 * <p>계층 규칙을 좁게 쓰면 이걸 위반으로 잡는다. <b>같은 계층 안의 호출은 정상이다.</b>
 * 실제 앱의 {@code BookingFacade → BookingService} 가 이 모양이다.
 */
@Fixture(rule = "R6", expect = Fixture.Expect.CLEAN,
         note = "Service → Service. 같은 계층 안의 호출은 허용돼야 한다")
@Service
public class OkFacade {

    private final OkService okService;

    public OkFacade(OkService okService) {
        this.okService = okService;
    }

    public long total() {
        return okService.count();
    }
}
