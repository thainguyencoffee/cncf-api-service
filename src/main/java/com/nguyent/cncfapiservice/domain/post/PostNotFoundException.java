package com.nguyent.cncfapiservice.domain.post;

public class PostNotFoundException extends RuntimeException{

    public PostNotFoundException(String uuid) {
        super("Could not find post with uuid: " + uuid);
    }
}
