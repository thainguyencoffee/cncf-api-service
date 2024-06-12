package com.nguyent.cncfapiservice.domain.interact;

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
public class InteractServiceImpl implements InteractService{

    private final InteractRepository interactRepository;
    private final PostRepository postRepository;

    public InteractServiceImpl(InteractRepository interactRepository, PostRepository postRepository) {
        this.interactRepository = interactRepository;
        this.postRepository = postRepository;
    }

    @Override
    public List<Interact> findAllInteractsByPostId(UUID postId, Pageable pageable) {
        log.info("InteractService.findAllInteractsByPostId postId {} (có phân trang {}/{})",
                postId, pageable.getPageNumber(), pageable.getPageSize());
        return Optional.of(interactRepository.findAllByPostId(postId, pageable))
                .filter(listInteract -> !listInteract.isEmpty())
                .orElseThrow(InteractEmptyException::new);
    }

    @Override
    public Interact findInteractByPostIdAndId(UUID interactId, UUID postId) {
        log.info("InteractService.findInteractByPostIdAndId interactId {}, postId {}", interactId, postId);
        var post = postRepository.findByIdAndDeleted(postId, false);
        if(post.isEmpty()) {
            throw new PostNotFoundException(postId.toString());
        }
        return interactRepository.findByIdAndPost(interactId, post.get())
                .orElseThrow(() -> new InteractNotFoundException(interactId));
    }

    @Override
    public Interact saveInteract(Interact interact) {
        return interactRepository.save(interact);
    }

    @Override
    public Interact updateEmoteByInteractIdAndUserId(UUID interactId, UUID userId, Interact interact) {
        int rowEffect = interactRepository.updateEmoteByIdAndUserId(interactId, userId, interact.getEmote());
        if(rowEffect == 0) {
            throw new InteractUpdateFailureException(interactId);
        }
        return interactRepository.findById(interactId)
                .orElseThrow(() -> new InteractNotFoundException(interactId));
    }

    @Override
    @Transactional
    public void deleteInteractById(UUID postId, UUID userId) {
        var post = postRepository.findByUserIdAndIdAndDeleted(userId, postId, false);
        if (post.isPresent()) {
            int cnt = interactRepository.deleteInteractByPostAndUserId(post.get(), userId);
            if (cnt == 0) {
                throw new DeleteInteractFailureException();
            }
        } else {
            throw new PostNotFoundException(postId.toString());
        }
    }
}
