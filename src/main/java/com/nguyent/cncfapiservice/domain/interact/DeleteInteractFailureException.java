package com.nguyent.cncfapiservice.domain.interact;

public class DeleteInteractFailureException extends RuntimeException {
    public DeleteInteractFailureException() {
        super("Haven't expressed any interact to this post yet or other errors!");
    }
}
