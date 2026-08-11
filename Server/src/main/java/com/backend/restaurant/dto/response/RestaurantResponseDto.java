package com.backend.restaurant.dto.response;

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
public class RestaurantResponseDto {

    private Long id;

    private Long ownerUserId;
    private String ownerUserName;

    private String name;
    private String city;
    private String address;
    private String contactNumber;
    private String contactEmail;
    private Integer priceBand;
    private Boolean active;    // true=OPEN, false=CLOSED (operational status)
    private Boolean deleted;   // true=soft-deleted/hidden, false=visible to customers
    private LocalDateTime createdAt;
}
