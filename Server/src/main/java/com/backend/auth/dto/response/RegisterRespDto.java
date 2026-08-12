package com.backend.auth.dto.response;

import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class RegisterRespDto {

    private Long   id;
    private String fullName;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;

    /*
     * A user can now hold multiple roles, so we return a Set of
     * role name strings instead of a single enum value.
     * Example JSON:  "roles": ["ROLE_CUSTOMER"]
     */
    private Set<String> roles;
}
