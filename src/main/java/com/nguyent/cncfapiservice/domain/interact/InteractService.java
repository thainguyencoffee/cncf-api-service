package com.nguyent.cncfapiservice.domain.interact;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InteractService {

    List<Interact> findAllInteractsByPostId(UUID postId, Pageable pageable);

    Interact saveInteract(Interact interact);

    void deleteInteractById(UUID postId, UUID userId);

    Interact findInteractByPostIdAndId(UUID interactId, UUID postId);

    Interact updateEmoteByInteractIdAndUserId(UUID interactId, UUID userId, Interact interact);
}
