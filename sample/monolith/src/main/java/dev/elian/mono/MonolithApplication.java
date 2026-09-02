package dev.elian.mono;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 좌석 예약 — 한 서버 안에 세 도메인(예약·알림·정산)을 둔다.
 *
 * <p>이 코드는 <b>규칙을 지킨 상태</b>다. 가드레일을 걸면 통과해야 한다.
 * 통과하지 못하면 규칙이 멀쩡한 코드를 잡는 것이므로 오탐이다.
 */
@SpringBootApplication
public class MonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonolithApplication.class, args);
    }
}
