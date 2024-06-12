package com.nguyent.cncfapiservice.domain.comment;

public class DeleteCommentFailureException extends RuntimeException{
    public DeleteCommentFailureException() {
        super("Haven't take any comment to this post yet or other errors!");
    }
}
