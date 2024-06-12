package com.nguyent.cncfapiservice.domain.user;

public class UserRepresentationNotFoundException extends RuntimeException {

    public UserRepresentationNotFoundException(String identify) {
        super("UserRepresentation not found for username/userid " + identify);
    }
}
