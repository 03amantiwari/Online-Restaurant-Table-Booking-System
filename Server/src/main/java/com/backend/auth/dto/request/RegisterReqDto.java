package com.backend.auth.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegisterReqDto {

    @NotBlank(message = "Name is required....")
    @Size(max = 80, message = "Name must be under 80 characters")
    private String fullName;

    @NotNull(message = "Date of Birth is required....")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is required....")
    @Email(message = "Email format invalid....")
    private String email;

    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[#@$*]).{8,20})",
             message = "Invalid password format!!!")
    @Size(min = 8, max = 20, message = "Password must be at least 8 and at most 20 characters")
    private String password;

    @NotBlank(message = "Phone Number is required....")
    private String phoneNumber;

    /*
     * @Pattern validates that only allowed role names are sent.
     * UserServiceImpl looks up the matching Role entity from the
     * roles table using RoleRepository.findByRoleName(roleName).
     */
    @NotBlank(message = "Role is required and cannot be blank")
    @Pattern(
        regexp  = "ROLE_ADMIN|ROLE_OWNER|ROLE_CUSTOMER",
        message = "Role must be one of: ROLE_ADMIN, ROLE_OWNER, ROLE_CUSTOMER"
    )
    private String roleName; 
}
