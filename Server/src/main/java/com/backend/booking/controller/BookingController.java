package com.backend.booking.controller;

import com.backend.booking.dto.request.BookingCreateRequestDto;
import com.backend.booking.dto.response.BookingResponseDto;
import com.backend.booking.dto.request.BookingStatusUpdateDto;
import com.backend.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ── Customer APIs ─────────────────────────────────────────────────────────

    // POST /api/bookings — create a new booking
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponseDto> createBooking(
            @Valid @RequestBody BookingCreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(dto));
    }

    // GET /api/bookings/my?userId=X — customer's own bookings
    @GetMapping("/bookings/my")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(@RequestParam Long userId) {
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }

    // PUT /api/bookings/{id}/cancel?userId=X — customer cancels their booking
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, userId));
    }

    // ── Restaurant Owner/Admin APIs ───────────────────────────────────────────

    // GET /api/restaurants/{restaurantId}/bookings — all bookings for a restaurant
    @GetMapping("/restaurants/{restaurantId}/bookings")
    public ResponseEntity<List<BookingResponseDto>> getRestaurantBookings(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(bookingService.getRestaurantBookings(restaurantId));
    }

    // PUT /api/bookings/{id}/status — owner updates booking status (CONFIRMED, CANCELLED, etc.)
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<BookingResponseDto> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusUpdateDto dto,
            @RequestParam(required = false) Long changedByUserId) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, dto, changedByUserId));
    }

    /*
     * REMOVED (dead endpoints — never called by frontend):
     *   GET /bookings/{id}                              getBookingById
     *   GET /restaurants/{restaurantId}/bookings/date/{date}     getBookingsByDate
     *   GET /restaurants/{restaurantId}/bookings/timeslot/{id}   getBookingsByTimeSlot
     */
}
