package com.nguyent.cncfapiservice.domain.comment;

import com.nguyent.cncfapiservice.domain.post.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotBlank(groups = OnCreate.class, message = "Content of comment must not be null")
    @Size(groups = {Post.OnCreate.class, Post.OnUpdate.class}, min = 1, max = 1000,
            message = "Comment size minimum is 1 character and maximum is 1000 characters")
    private String content;

    @Column(nullable = false)
    private UUID userId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @NotNull(message = "Post must not be null")
    private Post post;

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

    public static Comment of (String content, Post post) {
        return new Comment(null, content, null, post, null, null, null, null, 0);
    }

    public static interface OnCreate {}
    public static interface OnUpdate {}
}
