package com.nguyent.cncfapiservice.domain.interact;

import java.util.UUID;

public class InteractUpdateFailureException extends RuntimeException {
    public InteractUpdateFailureException(UUID interactId) {
        super("Could not update interact with id " + interactId);
    }
}
