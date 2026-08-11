package com.backend.restaurant.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Valid
public class RestaurantCreateRequestDto {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 100, message = "Restaurant name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "City name is required")
    private String city;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Contact number must be valid (10 to 15 digits, optional '+' prefix)")
    private String contactNumber;

    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @NotNull(message = "Price band is required")
    @Min(value = 1, message = "Price band minimum value is 1 (Budget)")
    @Max(value = 5, message = "Price band maximum value is 5 (Fine Dining)")
    private Integer priceBand;

    // 🔴 @Valid ensures that validation annotations inside each RestaurantTableCreateDto are evaluated
    @NotEmpty(message = "At least one table configuration must be provided")
    @Valid
    private List<RestaurantTableCreateDto> tables;

    // 🔴 @Valid ensures that validation annotations inside each TimeSlotCreateDto are evaluated
    @NotEmpty(message = "At least one time slot must be provided")
    @Valid
    private List<TimeSlotCreateDto> timeSlots;
}