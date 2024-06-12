package com.nguyent.cncfapiservice.domain.post;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PostService {

    /**
     * Retrieving all posts*/
    List<Post> getAllPosts(Pageable pageable);

    /**
     * Retrieving post by id*/
    Post getPostById(UUID postId);


    /**
     * Retrieving all posts by userId*/
    List<Post> getAllPostsByUserId(UUID userId, Pageable pageable);

    /**
     * Get post by userId and postId*/
    Post getPostByUserIdAndPostId(UUID userId, UUID postId);


    Post savePost(Post post);

    /**
     * Update post by userId and postId*/
    Post updatePostById(UUID userId, UUID postId, Post post);

    /**
     * Soft delete post by userId and postId*/
    void softDeletePostById(UUID userId, UUID postId);

    /**
     * Get deleted post by userId and postId */
    Post getDeletedPostById(UUID userId, UUID postId);

    /**
     * Retrieving all deleted posts by userId*/
    List<Post> getDeletedPostsByUserId(UUID userId, Pageable pageable);

    /**
     * Search posts by userId and content*/
    List<Post> searchPostsByContentAndUserId(UUID userid, String content, Pageable pageable);

    /**
     * Search posts by content*/
    List<Post> searchPosts(String content, Pageable pageable);

    /**
     * Search deleted posts by content and user id*/
    List<Post> searchDeletedPostsByContentAndUserId(String content, UUID userId, Pageable pageable);

    /**
     * Hồi phục một bài post đã xóa của chủ sở hữu dựa vào postId*/
    Post recoveryPostById(UUID userId, UUID postId);

    /**
     * Hồi phục toàn bộ bài post đã xóa của chủ sở hữu*/
    int recoveryAllPost(UUID userId);

}
