package com.nguyent.cncfapiservice.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByUserIdAndDeleted(UUID userId, boolean isDeleted, Pageable pageable);

    Optional<Post> findByUserIdAndIdAndDeleted(UUID userId, UUID postId, boolean isDeleted);

    Optional<Post> findByIdAndDeleted(UUID postId, boolean isDeleted);

    @Query("FROM Post a WHERE a.content LIKE  '%' || :content || '%' AND a.deleted = false")
    List<Post> searchPostByContent(@Param("content") String content, Pageable pageable);

    @Query("FROM Post a WHERE a.userId = :userId AND a.content LIKE  '%' || :content || '%' AND a.deleted = :deleted")
    List<Post> searchPostByContentAndUserIdAndDeleted(@Param("content") String content,
                                                      @Param("userId") UUID userId,
                                                      boolean deleted,
                                                      Pageable pageable);

    @Query("UPDATE Post a SET a.deleted = false, a.recoverDate = current_timestamp WHERE a.userId = :userId AND a.deleted = true")
    @Modifying
    @Transactional
    int recoveryAllPostByUserId(@Param("userId") UUID userId);
}
