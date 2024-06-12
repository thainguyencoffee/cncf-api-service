package com.nguyent.cncfapiservice.domain.comment;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    List<Comment> findAllCommentsByPostId(UUID postId, Pageable pageable);

    Comment findCommentByPostIdAndId(UUID commentId, UUID postId);

    Comment saveComment(Comment comment);

    Comment updateCommentByCommentIdAndUserId(UUID commentId, UUID userId, Comment comment);

    void deleteCommentByIdAndPostIdAndUserId(UUID commentId, UUID postId, UUID userId);
}
