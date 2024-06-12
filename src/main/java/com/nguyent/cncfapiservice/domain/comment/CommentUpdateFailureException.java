package com.nguyent.cncfapiservice.domain.comment;

import java.util.UUID;

public class CommentUpdateFailureException extends RuntimeException {
    public CommentUpdateFailureException(UUID commentId) {
        super("Could not update comment with id " + commentId);
    }
}
