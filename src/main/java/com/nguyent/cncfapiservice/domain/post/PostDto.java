package com.nguyent.cncfapiservice.domain.post;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    private UUID userId;

    @NotBlank(groups = Post.OnCreate.class, message = "Content must not be null or empty.")
    @Size(groups = {Post.OnCreate.class, Post.OnUpdate.class}, min = 1, max = 2000,
            message = "The post content must minimum length is 1 and maximum is 2000 characters")
    @Column(nullable = false)
    private String content;

    private List<MultipartFile> photos = new ArrayList<>();
}
