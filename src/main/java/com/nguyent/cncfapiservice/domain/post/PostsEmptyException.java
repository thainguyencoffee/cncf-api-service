package com.nguyent.cncfapiservice.domain.post;

public class PostsEmptyException extends RuntimeException{

    public PostsEmptyException() {
        super("List of posts is empty");
    }

}
