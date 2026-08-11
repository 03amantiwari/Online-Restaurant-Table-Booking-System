package com.backend.booking.dto.response;

import com.backend.booking.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusHistoryResponseDto {

    private Long id;
    private Long bookingId;

    // null on the very first timeline entry (booking created directly into its initial status)
    private Booking.BookingStatus fromStatus;
    private Booking.BookingStatus toStatus;

    // null if the change wasn't attributed to a specific user
    private Long changedByUserId;
    private String changedByUserName;

    private String note;
    private LocalDateTime changedAt;
}
