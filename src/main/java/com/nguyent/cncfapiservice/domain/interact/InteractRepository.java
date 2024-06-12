package com.nguyent.cncfapiservice.domain.interact;

import com.nguyent.cncfapiservice.domain.post.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InteractRepository extends JpaRepository<Interact, UUID> {

    List<Interact> findAllByPostId(UUID postId, Pageable pageable);

    Optional<Interact> findByIdAndPost(UUID id, Post post);

    @Query("UPDATE Interact a SET a.emote = :emote WHERE a.id = :id AND a.userId = :userId")
    @Modifying
    @Transactional
    int updateEmoteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId, @Param("emote") Interact.Emote emote);

    int deleteInteractByPostAndUserId(Post post, UUID userId);
}
