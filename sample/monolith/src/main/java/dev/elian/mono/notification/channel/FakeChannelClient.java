package dev.elian.mono.notification.channel;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** 실제 채널 대신 지연과 실패를 흉내낸다. */
@Component
public class FakeChannelClient implements ChannelClient {

    @Override
    public boolean send(String recipient, String message) {
        sleepQuietly(ThreadLocalRandom.current().nextInt(20, 80));   // 채널사 응답 대기
        return ThreadLocalRandom.current().nextInt(100) >= 10;        // 10% 실패
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
