package com.backend.restaurant.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Note: no ownerUserId here on purpose - a restaurant shouldn't be reassigned
// to a different owner through a general update (mirrors the RestaurantTable
// convention of not allowing restaurantId reassignment through its update DTO).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantUpdateRequestDto {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact number is required")
    private String contactNumber;

    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @Min(value = 1, message = "Price band must be between 1 and 5")
    @Max(value = 5, message = "Price band must be between 1 and 5")
    private Integer priceBand;

    @NotNull(message = "Active flag is required")
    private Boolean active;
}
