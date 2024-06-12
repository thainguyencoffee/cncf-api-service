package com.nguyent.cncfapiservice.domain.comment;

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

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<com.nguyent.cncfapiservice.domain.comment.Comment> findAllByPostId(UUID postId, Pageable pageable);

    Optional<Comment> findByIdAndPost(UUID id, Post post);

    @Query("UPDATE Comment a SET a.content = :content WHERE a.id = :id AND a.userId = :userId")
    @Modifying
    @Transactional
    int updateContentCommentByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId,
                                 @Param("content") String content);

    int deleteCommentByIdAndPostAndUserId(UUID commentId, Post post, UUID userId);
    
}
