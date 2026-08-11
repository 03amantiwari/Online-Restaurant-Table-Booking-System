package com.backend.booking.repository;

import com.backend.booking.entity.Booking;
import com.backend.booking.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {

   
    List<BookingStatusHistory> findByBookingOrderByChangedAtAsc(Booking booking);
}
