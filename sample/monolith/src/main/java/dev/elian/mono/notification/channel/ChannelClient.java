package dev.elian.mono.notification.channel;

/**
 * 외부 채널(푸시·SMS)로 나가는 호출.
 *
 * <p><b>이 호출은 트랜잭션 안에서 하면 안 된다(R3).</b> 응답 시간이 통제 밖이라
 * 그동안 락과 커넥션을 남이 정한 시간만큼 붙잡게 된다.
 */
public interface ChannelClient {

    /** @return 발송 성공 여부 */
    boolean send(String recipient, String message);
}
