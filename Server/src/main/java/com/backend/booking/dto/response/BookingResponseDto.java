package com.backend.booking.dto.response;

import com.backend.booking.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDto {

    private Long id;
    private String bookingReference;

    private Long userId;
    private String userFullName;

    private Long restaurantId;
    private String restaurantName;

    private Long tableId;
    private Integer tableNumber;

    private Long timeSlotId;
    private String timeSlotLabel;

    private LocalDate bookingDate;
    private Integer partySize;
    private Booking.BookingStatus status;
    private String specialRequest;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
}
