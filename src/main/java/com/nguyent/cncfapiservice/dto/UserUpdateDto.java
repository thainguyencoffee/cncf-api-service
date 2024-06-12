package com.nguyent.cncfapiservice.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserUpdateDto(
        String firstName,
        String lastName,
        String email,
        String birthdate,
        String gender,
        MultipartFile picture
) {
}
