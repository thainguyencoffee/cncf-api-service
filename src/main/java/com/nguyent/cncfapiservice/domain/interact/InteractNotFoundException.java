package com.nguyent.cncfapiservice.domain.interact;

import java.util.UUID;

public class InteractNotFoundException extends RuntimeException {
    public InteractNotFoundException(UUID interactId) {
        super("Could not find interact with id " + interactId);
    }
}
