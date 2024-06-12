package com.nguyent.cncfapiservice.domain.interact;

import com.nguyent.cncfapiservice.domain.post.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
public class Interact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Post post;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Emote of Interact must not be null")
    private Emote emote;

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

    public static Interact of(Post post, Emote emote) {
        return new Interact(null, null, post, emote, null, null, null, null, 0);
    }

    public static enum Emote {
        HAPPY, SAD
    }
}
