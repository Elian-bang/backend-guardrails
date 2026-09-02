package dev.elian.mono.booking.repository;

import dev.elian.mono.booking.domain.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {
}
