package com.nguyent.cncfapiservice.post;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.nguyent.cncfapiservice.config.DataConfig;
import com.nguyent.cncfapiservice.domain.post.Post;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DataConfig.class)
@Testcontainers
public class ApiServiceApplicationPostModuleTests {

    public static KeycloakToken userToken;
    public static KeycloakToken adminToken;
    protected static String USER_ID = "4a069652-5a37-4286-9da5-6248a734a989";

    @Container
    private static final PostgreSQLContainer<?> postgresql =
            new PostgreSQLContainer<>("postgres:16");

    @Container
    static KeycloakContainer keycloakContainer =
            new KeycloakContainer("quay.io/keycloak/keycloak:23.0")
                    .withRealmImportFile("chat-realm.json");
    @Autowired
    public WebTestClient webTestClient;
    public static String fakePost = "";

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);

        registry.add("keycloak.auth-server-url", keycloakContainer::getAuthServerUrl);
        registry.add("keycloak.realm", () -> "chat");
        registry.add("keycloak.resource", () -> "edge-service");
        registry.add("keycloak.credentials.secret", () -> "cT5pq7W3XStcuFVQMhjPbRj57Iqxcu4n");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> keycloakContainer.getAuthServerUrl() + "realms/chat");
    }

    /**
     * Phương thức khởi tạo đầu tiên khi run các test cases,
     * Thực hiện lấy access token cho hai account, một account với `user` role (user), và
     * một account với `admin` role (boss)
     */
    @BeforeAll
    static void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(keycloakContainer.getAuthServerUrl() + "realms/chat/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        userToken = authenticateWith("user", "1", webClient);
        adminToken = authenticateWith("boss", "1", webClient);
    }

    @BeforeEach
    void createFakeData() {
        // fake data
        Post post = Post.of("Fake content");
        MultiValueMap<String, String> postMultiValueMap = new LinkedMultiValueMap<String, String>();
        postMultiValueMap.add("content", post.getContent());
        fakePost = webTestClient.post()
                .uri("/posts", USER_ID)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken))
                .body(BodyInserters.fromMultipartData(postMultiValueMap))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext documentContext = JsonPath.parse(responseApi);
                    assertThat(documentContext.read("$.message", String.class))
                            .isEqualTo("Create new post successfully.");
                    assertThat(documentContext.read("$.data.userId", String.class))
                            .isNotNull();
                    assertThat(documentContext.read("$.data.createdBy", String.class))
                            .isNotNull();
                }).returnResult().getResponseBody();
    }
    /**
     * ================ Nơi viết các test cases ================
     */

    @Test
    void whenUnauthenticatedGetPostThenReturn() {
        webTestClient.get()
                .uri("/users/{userId}/posts", USER_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void whenUnauthenticatedCreateNewPostThenReturnUnauthenticated() {
        Post post = Post.of("This is content. This is content. This is content. This is content.");
        MultiValueMap<String, String> postMultiValueMap = new LinkedMultiValueMap<String, String>();
        postMultiValueMap.add("content", post.getContent());
        webTestClient.post()
                .uri("/posts")
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromMultipartData(postMultiValueMap))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext documentContext = JsonPath.parse(responseApi);
                    assertThat(documentContext.read("$.message", String.class))
                            .isEqualTo("Create new post successfully.");
                    assertThat(documentContext.read("$.data.userId", String.class))
                            .isNotNull();
                    assertThat(documentContext.read("$.data.createdBy", String.class))
                            .isNotNull();
                });

    }

    @Test
    void adminUpdatePostThenOK() {
        // create a demo post
        Post post = Post.of("This is content. This is content. This is content. This is content.");
        MultiValueMap<String, String> postMultiValueMap = new LinkedMultiValueMap<String, String>();
        postMultiValueMap.add("content", post.getContent());
        String postCreatedJson = webTestClient.post()
                .uri("/posts")
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromMultipartData(postMultiValueMap))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext documentContext = JsonPath.parse(responseApi);
                    assertThat(documentContext.read("$.message", String.class))
                            .isEqualTo("Create new post successfully.");
                    assertThat(documentContext.read("$.data.userId", String.class))
                            .isNotNull();
                    assertThat(documentContext.read("$.data.createdBy", String.class))
                            .isNotNull();
                }).returnResult().getResponseBody();

        String postIdCreated = JsonPath.parse(postCreatedJson).read("$.data.id");

        var content = "Content update";
        MultiValueMap<String, String> postUpdate = new LinkedMultiValueMap<String, String>();
        postUpdate.add("content", content);
        webTestClient.put()
                .uri("/posts/{id}", UUID.fromString(postIdCreated))
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromMultipartData(postUpdate))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    String updatedContent = JsonPath.parse(responseApi).read("$.data.content");
                    assertThat(updatedContent).isEqualTo(content);
                });

        webTestClient.put()
                .uri("/posts/{id}", UUID.fromString(postIdCreated))
                .headers(headers -> headers.setBearerAuth(adminToken.accessToken()))
                .body(BodyInserters.fromMultipartData(postUpdate))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenAuthenticatedUpdatePostThenOK() {
        // create a demo post
        Post post = Post.of("This is content. This is content. This is content. This is content.");
        MultiValueMap<String, String> postMultiValueMap = new LinkedMultiValueMap<String, String>();
        postMultiValueMap.add("content", post.getContent());
        String postCreatedJson = webTestClient.post()
                .uri("posts")
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromMultipartData(postMultiValueMap))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext documentContext = JsonPath.parse(responseApi);
                    assertThat(documentContext.read("$.message", String.class))
                            .isEqualTo("Create new post successfully.");
                    assertThat(documentContext.read("$.data.userId", String.class))
                            .isNotNull();
                    assertThat(documentContext.read("$.data.createdBy", String.class))
                            .isNotNull();
                }).returnResult().getResponseBody();

        String postIdCreated = JsonPath.parse(postCreatedJson).read("$.data.id");

        var content = "Content update";
        MultiValueMap<String, String> postUpdate = new LinkedMultiValueMap<String, String>();
        postUpdate.add("content", content);
        webTestClient.put()
                .uri("/posts/{id}",  postIdCreated)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromMultipartData(postUpdate))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    String updatedContent = JsonPath.parse(responseApi).read("$.data.content");
                    assertThat(updatedContent).isEqualTo(content);
                });
    }

    @Test
    void whenAuthenticatedDeletePostThenNoContent() {
        String id = JsonPath.parse(fakePost).read("$.data.id");

        webTestClient.delete()
                .uri("/posts/{id}", id)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isNoContent();
    }

    // tests case related to trash functional
    @Test
    void whenUnauthenticatedGetAllDeletedPostsThenReturnUnauthenticated() {
        webTestClient.get()
                .uri("/posts/trash")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedGetAllDeletedPostsThenReturnOK() {
        var postId = JsonPath.parse(fakePost).read("$.data.id");
        webTestClient.delete()
                .uri("/posts/{id}", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange().expectStatus().isNoContent();

        webTestClient.get()
                .uri("/posts/trash", "")
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    var message = JsonPath.parse(responseApi).read("$.message");
                    assertThat(message).isEqualTo("Retrieving all own deleted posts successfully.");
                    var data = JsonPath.parse(responseApi).read("$.data");
                    assertThat(data).isNotNull();
                });
    }

    @Test
    void whenUnauthenticatedSearchDeletedPostsThenReturnUnauthenticated() {
        webTestClient.get()
                .uri("/posts/trash/search")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedSearchDeletedPostsThenReturnOK() {
        var postId = JsonPath.parse(fakePost).read("$.data.id");
        webTestClient.delete()
                .uri("/posts/{id}", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange().expectStatus().isNoContent();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/posts/trash/search")
                        .queryParam("content", "Fake content")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build()
                )
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    var message = JsonPath.parse(responseApi).read("$.message");
                    assertThat(message).isEqualTo("Search own deleted posts by content successfully");
                    var data = JsonPath.parse(responseApi).read("$.data");
                    assertThat(data).isNotNull();
                });
    }

    @Test
    void whenAuthenticatedRecoveryPostByIdThenReturnPostEffected() {
        var postId = JsonPath.parse(fakePost).read("$.data.id");
        webTestClient.delete()
                .uri("/posts/{postId}", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.post()
                .uri("/posts/trash/recovery/{postId}", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    var message = JsonPath.parse(responseApi).read("$.message");
                    assertThat(message).isEqualTo("Recovery post by postId successfully.");
                    var deleted = JsonPath.parse(responseApi).read("$.data.deleted", Boolean.class);
                    Assertions.assertFalse(deleted);
                });
    }

    @Test
    void whenAuthenticatedRecoveryAllPostThenReturnRowCountEffected() {
        var postId = JsonPath.parse(fakePost).read("$.data.id");
        webTestClient.delete()
                .uri("/posts/{postId}", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.post()
                .uri("/posts/trash/recovery/all")
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    var message = JsonPath.parse(responseApi).read("$.message");
                    assertThat(message).isEqualTo("Recovery post by postId successfully. 1 rows effected.");
                });
    }


    /**
     * Phương thức để lấy access token
     */
    private static KeycloakToken authenticateWith(String username, String password, WebClient webClient) {
        return webClient
                .post()
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", "edge-service")
                        .with("client_secret", "cT5pq7W3XStcuFVQMhjPbRj57Iqxcu4n")
                        .with("username", username)
                        .with("password", password)
                ).retrieve()
                .bodyToMono(KeycloakToken.class)
                .block();
    }

    /**
     * Record này để lưu trữ access_token
     */
    public record KeycloakToken(String accessToken) {
        @JsonCreator
        public KeycloakToken(@JsonProperty("access_token") String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
