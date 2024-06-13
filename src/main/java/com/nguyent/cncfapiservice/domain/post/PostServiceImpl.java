package com.nguyent.cncfapiservice.domain.post;

import com.cloudinary.Cloudinary;
import com.nguyent.cncfapiservice.cloudinary.CloudinaryUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);
    private final PostRepository postRepository;
    private final Cloudinary cloudinary;

    @Override
    public List<Post> getAllPosts(Pageable pageable) {
        log.info("PostServiceImpl.getAllPosts");
        return postRepository.findAll(pageable).getContent();
    }

    @Override
    public Post getPostById(UUID postId) {
        log.info("PostServiceImpl.getPostById postId: {}", postId);
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
    }

    public List<Post> getAllPostsByUserId(UUID userId, Pageable pageable) {
        log.info("PostServiceImpl.getAllPosts userId {}, pageable with offset {} and pageSize {}",
                userId, pageable.getOffset(), pageable.getPageSize());
        return Optional.of(postRepository.findAllByUserIdAndDeleted(userId, false, pageable).getContent())
                .filter(listPosts -> !listPosts.isEmpty())
                .orElseThrow(PostsEmptyException::new);
    }

    @Override
    public Post getPostByUserIdAndPostId(UUID userId, UUID postId) {
        log.info("PostServiceImpl.getPostById userId {} postId {}",userId.toString(), postId.toString());
        return postRepository.findByUserIdAndIdAndDeleted(userId, postId, false)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
    }

    @Override
    public Post savePost(PostDto postDto) {
        log.info("PostServiceImpl.savePost");
        var post = new Post();
        post.setUserId(postDto.getUserId());
        post.setContent(postDto.getContent());
        post.setPhotos(CloudinaryUtils.convertListMultipartFileToListUrl(postDto.getPhotos(), cloudinary));
        return postRepository.save(post);
    }

    public List<Post> searchPosts(String content, Pageable pageable) {
        return Optional.of(postRepository.searchPostByContent(content, pageable))
                .filter(listPosts -> !listPosts.isEmpty())
                .orElseThrow(PostsEmptyException::new);
    }

    public List<Post> searchPostsByContentAndUserId(UUID userId, String content, Pageable pageable) {
        return Optional.of(postRepository.searchPostByContentAndUserIdAndDeleted(content, userId, false, pageable))
                .filter(listPosts -> !listPosts.isEmpty())
                .orElseThrow(PostsEmptyException::new);
    }

    public List<Post> getDeletedPostsByUserId(UUID userId, Pageable pageable) {
        return Optional.of(postRepository.findAllByUserIdAndDeleted(userId, true, pageable).getContent())
                .filter(listPosts -> !listPosts.isEmpty())
                .orElseThrow(PostsEmptyException::new);
    }

    public Post getDeletedPostById(UUID userId, UUID postId) {
        return postRepository.findByUserIdAndIdAndDeleted(userId, postId, true)
                .orElseThrow(() -> new PostNotFoundException(postId.toString()));
    }

    public Post updatePostById(UUID userId, UUID postId, PostDto postDto) {
        Post postPersisted = getPostByUserIdAndPostId(userId, postId);
        var postUpdate = new Post();
        postUpdate.setId(postPersisted.getId());
        postUpdate.setUserId(postPersisted.getUserId());
        postUpdate.setCreatedDate(postPersisted.getCreatedDate());
        postUpdate.setCreatedBy(postPersisted.getCreatedBy());
        postUpdate.setVersion(postPersisted.getVersion());
        // update content
        postUpdate.setContent(
                Optional.ofNullable(postDto.getContent())
                        .orElse(postPersisted.getContent()));
        // update photos
        postUpdate.setPhotos(Optional.of(postDto.getPhotos())
                .filter(photos -> !photos.isEmpty())
                .map(multipartFiles -> {
                    // handle delete old photos
                    List<String> oldPhotos = postPersisted.getPhotos();
                    if (!oldPhotos.isEmpty()) {
                        oldPhotos.forEach(oldPhoto -> {
                            var publicId = CloudinaryUtils.convertUrlToPublicId(oldPhoto);
                            log.info("oldPhoto {}", oldPhoto);
                            log.info("publicId {}", publicId);
                            String status = null;
                            try {
                                status = CloudinaryUtils.deleteFile(publicId, cloudinary);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            log.info("deleteFile {}", status);
                        });
                    }
                    // upload new picture
                    return CloudinaryUtils.convertListMultipartFileToListUrl(multipartFiles, cloudinary);
                }).orElseGet(postPersisted::getPhotos));
        return postRepository.save(postUpdate);
    }

    public void softDeletePostById(UUID userId, UUID postId) {
        Post postDeleted = getPostByUserIdAndPostId(userId, postId);
        postDeleted.setDeleted(true);
        postDeleted.setDeletedDate(Instant.now());
        postRepository.save(postDeleted);
        if (!postDeleted.isDeleted()) {
            throw new RuntimeException("Post with id " + postId + " and userId " + userId + " deleted failure!.");
        }
    }

    @Override
    public List<Post> searchDeletedPostsByContentAndUserId(String content, UUID userId, Pageable pageable) {
        return Optional.ofNullable(postRepository.searchPostByContentAndUserIdAndDeleted(content, userId, true, pageable))
                .filter(posts -> !posts.isEmpty())
                .orElseThrow(PostsEmptyException::new);
    }

    @Override
    @Transactional
    public Post recoveryPostById(UUID userId, UUID postId) {
        log.info("PostServiceImpl.recoverPostById userId {}, postId {}", userId, postId);
        Post post = getDeletedPostById(userId, postId);
        post.setDeleted(false);
        post.setRecoverDate(Instant.now());
        var postRecovered = postRepository.save(post);
        if (postRecovered.isDeleted()) {
            throw new RuntimeException("Post with id " + postId + " and userId " + userId + " recovered failure!.");
        }
        return postRecovered;
    }

    @Override
    public int recoveryAllPost(UUID userId) {
        log.info("PostServiceImpl.recoverAllPost userId {}", userId);
        return postRepository.recoveryAllPostByUserId(userId);
    }

}
