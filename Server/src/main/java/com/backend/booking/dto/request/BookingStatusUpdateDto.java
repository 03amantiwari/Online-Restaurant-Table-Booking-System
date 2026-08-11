package com.backend.booking.dto.request;

import com.backend.booking.entity.Booking;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusUpdateDto {

    @NotNull(message = "Status is required")
    private Booking.BookingStatus status;
}
