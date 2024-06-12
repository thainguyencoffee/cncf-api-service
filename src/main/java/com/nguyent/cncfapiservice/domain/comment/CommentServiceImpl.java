package com.nguyent.cncfapiservice.domain.comment;

import com.nguyent.cncfapiservice.domain.interact.InteractEmptyException;
import com.nguyent.cncfapiservice.domain.post.PostNotFoundException;
import com.nguyent.cncfapiservice.domain.post.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Override
    public List<Comment> findAllCommentsByPostId(UUID postId, Pageable pageable) {
        log.info("CommentService.findAllCommentsByPostId postId {} (có phân trang {}/{})",
                postId, pageable.getPageNumber(), pageable.getPageSize());
        return Optional.of(commentRepository.findAllByPostId(postId, pageable))
                .filter(listComment -> !listComment.isEmpty())
                .orElseThrow(InteractEmptyException::new);
    }

    @Override
    public Comment findCommentByPostIdAndId(UUID commentId, UUID postId) {
        log.info("CommentService.findCommentByPostIdAndId commentId {}, postId {}", commentId, postId);
        var post = postRepository.findByIdAndDeleted(postId, false);
        if(post.isEmpty()) {
            throw new PostNotFoundException(postId.toString());
        }
        return commentRepository.findByIdAndPost(commentId, post.get())
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    @Override
    public Comment saveComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Comment updateCommentByCommentIdAndUserId(UUID commentId, UUID userId, Comment comment) {
        int rowEffect = commentRepository.updateContentCommentByIdAndUserId(
                commentId, userId, comment.getContent());
        if(rowEffect == 0) {
            throw new CommentUpdateFailureException(commentId);
        }
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    @Override
    @Transactional
    public void deleteCommentByIdAndPostIdAndUserId(UUID commentId, UUID postId, UUID userId) {
        var post = postRepository.findByUserIdAndIdAndDeleted(userId, postId,false);
        if (post.isPresent()) {
            int cnt = commentRepository.deleteCommentByIdAndPostAndUserId(commentId, post.get(), userId);
            if (cnt == 0) {
                throw new DeleteCommentFailureException();
            }
        } else {
            throw new PostNotFoundException(postId.toString());
        }
    }
}
