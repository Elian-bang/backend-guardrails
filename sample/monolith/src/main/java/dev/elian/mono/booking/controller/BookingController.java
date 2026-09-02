package dev.elian.mono.booking.controller;

import dev.elian.mono.booking.domain.Booking;
import dev.elian.mono.booking.domain.Seat;
import dev.elian.mono.booking.service.BookingFacade;
import dev.elian.mono.booking.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <b>{@code @Transactional} 이 없다(R1).</b> 트랜잭션 경계는 서비스가 연다.
 * <b>Repository 를 직접 부르지 않는다(R6).</b>
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingFacade bookingFacade;
    private final BookingService bookingService;

    public BookingController(BookingFacade bookingFacade, BookingService bookingService) {
        this.bookingFacade = bookingFacade;
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse book(@RequestBody BookingRequest request) {
        Booking booking = bookingFacade.book(request.showId(), request.seatNo(), request.customerId());
        return BookingResponse.from(booking);
    }

    @GetMapping
    public List<BookingResponse> myBookings(@RequestParam String customerId) {
        return bookingService.findByCustomer(customerId).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/shows/{showId}/seats")
    public List<SeatResponse> seats(@PathVariable Long showId) {
        return bookingService.findSeats(showId).stream().map(SeatResponse::from).toList();
    }

    @DeleteMapping("/{bookingId}")
    public void cancel(@PathVariable Long bookingId) {
        bookingService.cancel(bookingId);
    }

    public record BookingRequest(Long showId, String seatNo, String customerId) {}

    public record BookingResponse(Long id, Long showId, Long seatId, String status, int amount) {
        static BookingResponse from(Booking b) {
            return new BookingResponse(b.getId(), b.getShowId(), b.getSeatId(), b.getStatus().name(), b.getAmount());
        }
    }

    public record SeatResponse(Long id, String seatNo, String status) {
        static SeatResponse from(Seat s) {
            return new SeatResponse(s.getId(), s.getSeatNo(), s.getStatus().name());
        }
    }
}
