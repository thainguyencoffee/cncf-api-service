package com.nguyent.cncfapiservice.dto;

public record UserUpdateDto(
        String firstName,
        String lastName,
        String email,
        String birthdate,
        String gender
) {
}
