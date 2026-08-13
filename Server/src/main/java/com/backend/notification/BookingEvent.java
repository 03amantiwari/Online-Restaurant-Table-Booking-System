package com.backend.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEvent {

    private UUID eventId;

    private String userName;
    
    
    private String restaurantName;

    private String createdByEmail;
    
    

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime occurredOn;

}
