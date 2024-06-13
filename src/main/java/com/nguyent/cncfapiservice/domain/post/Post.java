package com.nguyent.cncfapiservice.domain.post;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(groups = OnCreate.class, message = "Content must not be null or empty.")
    @Size(groups = {OnCreate.class, OnUpdate.class}, min = 1, max = 2000,
            message = "The post content must minimum length is 1 and maximum is 2000 characters")
    @Column(nullable = false)
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_photos", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "photo", nullable = false)
    private List<String> photos = new ArrayList<>();

    @Column(nullable = false)
    private UUID userId;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastModifiedDate;

    @Version
    private int version;
    @Column(nullable = false)
    private boolean deleted = false;
    private Instant deletedDate;
    private Instant recoverDate;

    public static Post of (String content) {
        return new Post(null, content,
                null, null,
                null, null,
                null, null,
                0, false,
                null,
                null);
    }

    public static Post reference(UUID postId) {
        return new Post(postId, null,
                null, null,
                null, null,
                null, null,
                0, false,
                null,
                null);
    }

    public static interface OnCreate {}
    public static interface OnUpdate {}
}
