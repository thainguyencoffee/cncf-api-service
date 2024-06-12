package com.nguyent.cncfapiservice.domain.comment;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException{
    public CommentNotFoundException(UUID commentId) {
        super("Comment with id " + commentId + " not found");
    }
}
