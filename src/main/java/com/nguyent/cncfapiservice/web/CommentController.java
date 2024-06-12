package com.nguyent.cncfapiservice.web;

import com.nguyent.cncfapiservice.domain.comment.Comment;
import com.nguyent.cncfapiservice.domain.comment.CommentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(produces = "application/json")
@Slf4j
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getAllCommentsByPostId(@PathVariable UUID postId, Pageable pageable) {
        log.info("Nhận toàn bộ bình luận của một bài post (có phân trang) postId {}", postId);
        List<Comment> allCommentsByPostId = commentService.findAllCommentsByPostId(postId, pageable);
        return new ResponseApi("OK", 200,
                "Get all comments for specific post by postId successfully.", null,
                allCommentsByPostId);
    }

    @GetMapping("/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getCommentByIdAndPostId(@PathVariable UUID postId, @PathVariable UUID commentId) {
        log.info("Nhận thông tin chi tiết về một bình luận dựa vào commentId {}", commentId);
        Comment commentById = commentService.findCommentByPostIdAndId(commentId, postId);
        return new ResponseApi("OK", 200,
                "Get comment for specific post by commentId successfully.",
                null, commentById);
    }

    @PostMapping(value = "/comments", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseApi createComment(@Valid @RequestBody Comment comment, @AuthenticationPrincipal Jwt jwt) {
        log.info("Bình luận tới một bài post theo postId {}", comment.getPost().getId());
        comment.setUserId(UUID.fromString(jwt.getSubject()));
        Comment commentSaved = commentService.saveComment(comment);
        return new ResponseApi("CREATED", 201,
                "Take a new comment to a post successfully.", null, commentSaved);
    }

    @PutMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi updateCommentByIdAndUserId(@PathVariable UUID commentId,
                                                   @RequestBody Comment comment,
                                                   @AuthenticationPrincipal Jwt jwt) {
        log.info("Chỉnh sửa bình luận dựa vào commentId {}", commentId);
        Comment commentUpdate = commentService.updateCommentByCommentIdAndUserId(
                commentId,
                UUID.fromString(jwt.getSubject()),
                comment
        );
        return new ResponseApi("OK", 200,
                "Update comment by commentId successfully.", null, commentUpdate);
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseApi deleteCommentByPostId(@PathVariable UUID postId,
                                              @AuthenticationPrincipal Jwt jwt,
                                              @PathVariable UUID commentId) {
        log.info("Xóa bình luận từ một bài post theo postId {} và commentId {}", postId, commentId);
        commentService.deleteCommentByIdAndPostIdAndUserId(commentId, postId,
                UUID.fromString(jwt.getSubject()));
        return new ResponseApi("No Content", 204,
                "Delete interact by postId and userId successfully.", null, null);
    }
    
}
