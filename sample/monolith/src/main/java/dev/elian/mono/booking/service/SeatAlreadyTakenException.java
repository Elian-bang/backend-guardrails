package dev.elian.mono.booking.service;

public class SeatAlreadyTakenException extends RuntimeException {
    public SeatAlreadyTakenException(Long showId, String seatNo) {
        super("이미 잡힌 좌석입니다: show=%d seat=%s".formatted(showId, seatNo));
    }
}
