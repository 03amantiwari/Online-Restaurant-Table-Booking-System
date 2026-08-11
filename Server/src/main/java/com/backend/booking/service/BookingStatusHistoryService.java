package com.backend.booking.service;

import com.backend.booking.dto.response.BookingStatusHistoryResponseDto;
import com.backend.booking.entity.Booking;
import com.backend.user.entity.User;

import java.util.List;

public interface BookingStatusHistoryService {

    // ---- Public read API ----
    List<BookingStatusHistoryResponseDto> getHistoryForBooking(Long bookingId);

    // ---- Internal only - called from BookingService whenever a booking's status changes.
    // Not exposed via any controller (no Create/Update/Delete API per spec).
    void recordStatusChange(Booking booking, Booking.BookingStatus fromStatus,
                             Booking.BookingStatus toStatus, User changedByUser, String note);
}
