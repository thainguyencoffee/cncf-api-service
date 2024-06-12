package com.nguyent.cncfapiservice.domain.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String birthdate;
    private String gender;
    private List<String> roles;
}
