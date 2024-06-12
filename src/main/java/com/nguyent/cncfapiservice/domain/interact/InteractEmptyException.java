package com.nguyent.cncfapiservice.domain.interact;

public class InteractEmptyException extends RuntimeException{

    public InteractEmptyException() {
        super("List interacts is empty.");
    }

}
