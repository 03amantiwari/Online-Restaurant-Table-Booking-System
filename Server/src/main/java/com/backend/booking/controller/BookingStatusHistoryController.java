package com.backend.booking.controller;

import com.backend.booking.dto.response.BookingStatusHistoryResponseDto;
import com.backend.booking.service.BookingStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Read-only by design - no POST/PUT/DELETE here. Rows are written internally by
// BookingService whenever a booking's status actually changes.
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingStatusHistoryController {

    private final BookingStatusHistoryService bookingStatusHistoryService;

    @GetMapping("/bookings/{bookingId}/history")
    public ResponseEntity<List<BookingStatusHistoryResponseDto>> getHistory(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingStatusHistoryService.getHistoryForBooking(bookingId));
    }
}
