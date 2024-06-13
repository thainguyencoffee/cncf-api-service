package com.nguyent.cncfapiservice.web;

import com.nguyent.cncfapiservice.domain.post.Post;
import com.nguyent.cncfapiservice.domain.post.PostDto;
import com.nguyent.cncfapiservice.domain.post.PostNotFoundException;
import com.nguyent.cncfapiservice.domain.post.PostService;
import com.nguyent.cncfapiservice.domain.user.UserRepresentationNotFoundException;
import com.nguyent.cncfapiservice.domain.user.UserService;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(produces = "application/json")
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final UserService userService;

    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }

    @GetMapping("/posts")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getAllPost(Pageable pageable) {
        log.info("Nhận toàn bộ bài post (có phân trang)");
        var posts = postService.getAllPosts(pageable);
        return new ResponseApi("OK", 200,
                "Retrieving all posts successfully.", null, posts);
    }

    @GetMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getPostById(@PathVariable UUID postId) {
        log.info("Nhận một bài post dựa vào postId {}", postId);
        var postById = postService.getPostById(postId);
        return new ResponseApi("OK", 200,
                "Retrieving post successfully.", null, postById);
    }

    @GetMapping("/users/{userId}/posts")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getAllPostsByUserId(@PathVariable UUID userId, Pageable pageable) {
        log.info("Nhận toàn bộ bài post của một người dùng bất kỳ dựa vào userId và postId");
        try {
            userService.getUserById(userId.toString());
            List<Post> posts = postService.getAllPostsByUserId(userId, pageable);
            return new ResponseApi("OK", 200,
                    "Retrieving all posts by userId successfully.", null, posts);
        } catch (NotFoundException ex) {
            throw new UserRepresentationNotFoundException(userId.toString());
        }
    }

    @GetMapping("/users/{userId}/posts/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getPostsByPostIdAndUserId(@PathVariable UUID userId, @PathVariable UUID postId) {
        log.info("Nhận một bài post dựa vào userId {} và postId {}", userId, postId);
        Post posts = postService.getPostByUserIdAndPostId(userId, postId);
        return new ResponseApi("OK", 200,
                "Retrieving a post by postId successfully.", null, posts);
    }

    @GetMapping("/users/{userId}/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi searchPostsByContentAndUserId(@RequestParam String content,
                                                     @PathVariable UUID userId,
                                                     Pageable pageable) {
        log.info("Tìm kiếm các bài posts dựa vào content={} và userId={}", content, userId);
        return new ResponseApi("OK", 200, "Search posts by userId and content successfully",
                null, postService.searchPostsByContentAndUserId(userId, content, pageable));
    }

    @GetMapping("/posts/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi searchPostsByContent(@RequestParam String content,
                                            Pageable pageable) {
        log.info("Tìm kiếm các bài posts dựa vào content={}", content);
        return new ResponseApi("OK", 200, "Search posts by content successfully",
                null, postService.searchPosts(content, pageable));
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseApi createPost(@Validated(Post.OnCreate.class) @ModelAttribute PostDto postDto, @AuthenticationPrincipal Jwt jwt) {
        log.info("Tạo một bài post cho chủ sở hữu");
        postDto.setUserId(UUID.fromString(jwt.getSubject()));
        var postCreated = postService.savePost(postDto);
        return new ResponseApi("CREATED", 201,
                "Create new post successfully.", null, postCreated);
    }

    @PutMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi updateOwnPost(@PathVariable UUID postId,
                                     @AuthenticationPrincipal Jwt jwt,
                                     @Validated(Post.OnUpdate.class) @ModelAttribute PostDto postDto)
            throws PostNotFoundException {
        log.info("Cập nhật một bài post của chủ sở hữu, postId {}", postId.toString());
        postService.getPostByUserIdAndPostId(UUID.fromString(jwt.getSubject()), postId);
        return new ResponseApi("OK", 200,
                "Update post with id " + postId.toString() + " successfully.",
                null, postService.updatePostById(UUID.fromString(jwt.getSubject()), postId, postDto));
    }

    @DeleteMapping("/posts/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseApi deletePost(@PathVariable UUID postId, @AuthenticationPrincipal Jwt jwt) {
        log.info("Xóa một bài post của chủ sở hữu, postId {}", postId.toString());
        postService.softDeletePostById(UUID.fromString(jwt.getSubject()), postId);
        return new ResponseApi("No Content", 204,
                "Deleted Post with id " + postId.toString() + " successfully", null, null);
    }

    @GetMapping("/posts/trash")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getAllPostsDeleted(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        log.info("Nhận toàn bộ bài post đã xóa của chủ sở hữu (có phân trang)");
        List<Post> posts = postService.getDeletedPostsByUserId(UUID.fromString(jwt.getSubject()), pageable);
        return new ResponseApi("OK", 200,
                "Retrieving all own deleted posts successfully.", null, posts);
    }

    @GetMapping("/posts/trash/{postId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getDeletedPostById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        log.info("Nhận một bài post đã xóa của chủ sở hữu dựa vào postId {}", postId.toString());
        return new ResponseApi("OK", 200,
                "Retrieving own deleted post by postId " + postId.toString() + " successfully.", null,
                postService.getDeletedPostById(UUID.fromString(jwt.getSubject()), postId));
    }

    @GetMapping("/posts/trash/search")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi searchOwnDeletedPostsByContent(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam String content,
                                                      Pageable pageable) {
        log.info("Tìm kiếm tất cả các bài posts đã xóa của chủ sở hữu dựa vào content");
        return new ResponseApi("OK", 200, "Search own deleted posts by content successfully",
                null, postService.searchDeletedPostsByContentAndUserId(
                content, UUID.fromString(jwt.getSubject()), pageable));
    }

    @PostMapping("/posts/trash/recovery/{postId}")
    public ResponseApi recoverPostById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID postId) {
        log.info("Hồi phục một bài post đã xóa của chủ sở hữu dựa vào postId {}", postId.toString());
        Post postRecovered = postService.recoveryPostById(UUID.fromString(jwt.getSubject()), postId);
        return new ResponseApi("OK", 200,
                "Recovery post by postId successfully.", null, postRecovered);
    }

    @PostMapping("/posts/trash/recovery/all")
    public ResponseApi recoveryAllPost(@AuthenticationPrincipal Jwt jwt) {
        log.info("Hồi phục toàn bộ bài post đã xóa của chủ sở hữu");
        int countEffect = postService.recoveryAllPost(UUID.fromString(jwt.getSubject()));
        return new ResponseApi("OK", 200,
                "Recovery post by postId successfully. " + countEffect + " rows effected.", null, null);
    }

}
