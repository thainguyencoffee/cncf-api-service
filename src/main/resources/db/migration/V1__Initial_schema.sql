CREATE TABLE post
(
    id                 UUID                        NOT NULL,
    content            VARCHAR(2000)               NOT NULL,
    user_id            UUID                        NOT NULL,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version            INTEGER                     NOT NULL,
    deleted            BOOLEAN                     NOT NULL,
    deleted_date       TIMESTAMP WITHOUT TIME ZONE,
    recover_date       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_post PRIMARY KEY (id)
);

CREATE TABLE interact
(
    id                 UUID                        NOT NULL,
    user_id            UUID                        NOT NULL,
    post_id            UUID                        NOT NULL,
    emote              VARCHAR(255)                NOT NULL,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version            INTEGER                     NOT NULL,
    CONSTRAINT pk_interact PRIMARY KEY (id)
);

ALTER TABLE interact
    ADD CONSTRAINT FK_INTERACT_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

CREATE TABLE comment
(
    id                 UUID                        NOT NULL,
    content            VARCHAR(1000)               NOT NULL,
    user_id            UUID                        NOT NULL,
    post_id            UUID                        NOT NULL,
    created_by         VARCHAR(255),
    created_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_modified_by   VARCHAR(255),
    last_modified_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    version            INTEGER                     NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);