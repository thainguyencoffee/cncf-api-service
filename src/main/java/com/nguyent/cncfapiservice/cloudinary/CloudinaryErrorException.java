package com.nguyent.cncfapiservice.cloudinary;

public class CloudinaryErrorException extends RuntimeException{

    public CloudinaryErrorException(String message) {
        super(message);
    }
}
