package com.fabish.hotel.repository;

import com.fabish.hotel.model.Booking;
import com.fabish.hotel.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByGuestId(Long guestId);
    List<Booking> findByRoomId(Long roomId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByCheckInBetween(LocalDateTime start, LocalDateTime end);
} 