package com.backend.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequestDto {

    // TEMPORARY: until the auth/JWT module exists, the caller must tell us who
    // they are. Once security is wired in, drop this field and read the user
    // from the authenticated principal instead (do NOT trust a client-supplied
    // user id for that going forward).
    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotNull(message = "Table id is required")
    private Long tableId;

    @NotNull(message = "Time slot id is required")
    private Long timeSlotId;

    @NotNull(message = "Booking date is required")
    @FutureOrPresent(message = "Booking date cannot be in the past")
    private LocalDate bookingDate;

    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be greater than 0")
    private Integer partySize;

    private String specialRequest;
}
