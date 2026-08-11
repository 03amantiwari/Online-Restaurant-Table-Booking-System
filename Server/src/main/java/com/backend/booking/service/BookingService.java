package com.backend.booking.service;

import com.backend.booking.dto.request.BookingCreateRequestDto;
import com.backend.booking.dto.request.BookingStatusUpdateDto;
import com.backend.booking.dto.response.BookingResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    // ---- Customer ----
    BookingResponseDto createBooking(BookingCreateRequestDto dto);

    BookingResponseDto getBookingById(Long bookingId);

    List<BookingResponseDto> getMyBookings(Long userId);

    BookingResponseDto cancelBooking(Long bookingId, Long userId);

    // ---- Restaurant Owner/Admin ----
    List<BookingResponseDto> getRestaurantBookings(Long restaurantId);

    // changedByUserId is optional (TEMPORARY placeholder until JWT auth exists - see
    // BookingCreateRequestDto note). Pass null if the actor isn't known/available.
    BookingResponseDto updateBookingStatus(Long bookingId, BookingStatusUpdateDto dto, Long changedByUserId);

    List<BookingResponseDto> getBookingsByDate(Long restaurantId, LocalDate date);

    List<BookingResponseDto> getBookingsByTimeSlot(Long restaurantId, Long timeSlotId);
}