package com.nguyent.cncfapiservice.web;

import com.nguyent.cncfapiservice.domain.interact.Interact;
import com.nguyent.cncfapiservice.domain.interact.InteractService;
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
public class InteractController {

    private final InteractService interactService;

    public InteractController(InteractService interactService) {
        this.interactService = interactService;
    }

    @GetMapping("/posts/{postId}/interacts")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getAllInteractByPostId(@PathVariable UUID postId, Pageable pageable) {
        log.info("Nhận toàn bộ tương tác biểu cảm của một bài post theo postId {}", postId);
        List<Interact> allInteractsByPostId = interactService.findAllInteractsByPostId(postId, pageable);
        return new ResponseApi("OK", 200,
                "Get all interacts for specific post by postId successfully.", null,
                allInteractsByPostId);
    }

    @GetMapping("/posts/{postId}/interacts/{interactId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getInteractByIdAndPostId(@PathVariable UUID postId, @PathVariable UUID interactId) {
        log.info("Nhận thông tin chi tiết về một tương tác biểu cảm dựa vào interactId {}", interactId);
        Interact interactById = interactService.findInteractByPostIdAndId(interactId, postId);
        return new ResponseApi("OK", 200,
                "Get interact for specific post by interactId successfully.",
                null, interactById);
    }

    @PostMapping(value = "/interacts", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseApi createInteract(@Valid @RequestBody Interact interact, @AuthenticationPrincipal Jwt jwt) {
        log.info("Bày tỏ cảm xúc tới một bài post theo postId {}", interact.getPost().getId());
        interact.setUserId(UUID.fromString(jwt.getSubject()));
        Interact interactSaved = interactService.saveInteract(interact);
        return new ResponseApi("CREATED", 201,
                "Expressing your emotion to a post successfully.", null, interactSaved);
    }

    @PutMapping("/interacts/{interactId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi updateInteractByIdAndUserId(@PathVariable UUID interactId,
                                                   @RequestBody Interact interact,
                                                   @AuthenticationPrincipal Jwt jwt) {
        log.info("Chỉnh sửa cảm xúc dựa vào interactId {}", interactId);
        Interact interactUpdate = interactService.updateEmoteByInteractIdAndUserId(
                interactId,
                UUID.fromString(jwt.getSubject()),
                interact
        );
        return new ResponseApi("OK", 200,
                "Update emote for a interact successfully.", null, interactUpdate);
    }

    @DeleteMapping("/posts/{postId}/interacts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseApi deleteInteractByPostId(@PathVariable UUID postId, @AuthenticationPrincipal Jwt jwt) {
        log.info("Hủy bày tỏ cảm xúc tới một bài post theo postId {}", postId);
        interactService.deleteInteractById(postId, UUID.fromString(jwt.getSubject()));
        return new ResponseApi("No Content", 204,
                "Delete interact by postId and userId successfully.", null, null);
    }
}
