package com.nguyent.cncfapiservice.interact;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.nguyent.cncfapiservice.config.DataConfig;
import com.nguyent.cncfapiservice.domain.interact.Interact;
import com.nguyent.cncfapiservice.domain.post.Post;
import dasniko.testcontainers.keycloak.KeycloakContainer;
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
public class ApiServiceApplicationInteractModuleTests {

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
    void whenUnAuthenticatedExpressPostThenReturnUnauthenticated() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        Interact interact = Interact.of(Post.reference(postId), Interact.Emote.HAPPY);

        webTestClient.post()
                .uri("/interacts")
                .body(BodyInserters.fromValue(interact))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedExpressPostThenCreated() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        Interact interact = Interact.of(Post.reference(postId), Interact.Emote.HAPPY);

        webTestClient.post()
                .uri("/interacts")
                .body(BodyInserters.fromValue(interact))
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(responseApi -> {
                    String msg = JsonPath.parse(responseApi).read("$.message", String.class);
                    assertThat(msg).isEqualTo("Expressing your emotion to a post successfully.");
                });
    }

    @Test
    void whenUnauthenticatedGetAllInteractsOfPostHasNoInteractThenReturnNotFound() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        webTestClient.get()
                .uri("/posts/{postId}/interacts", postId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUnauthenticatedGetAllInteractsOfPostThenReturnOK() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        Interact interact = Interact.of(Post.reference(postId), Interact.Emote.HAPPY);

        webTestClient.post()
                .uri("/interacts")
                .body(BodyInserters.fromValue(interact))
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .value(responseApi -> {
                    String msg = JsonPath.parse(responseApi).read("$.message", String.class);
                    assertThat(msg).isEqualTo("Expressing your emotion to a post successfully.");
                });

        webTestClient.get()
                .uri("/posts/{postId}/interacts", postId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    String msg = JsonPath.parse(responseApi).read("$.message", String.class);
                    assertThat(msg).isEqualTo("Get all interacts for specific post by postId successfully.");
                    String idInteract = JsonPath.parse(responseApi).read("$.data[0].id", String.class);
                    assertThat(idInteract).isNotNull();
                });
    }

    @Test
    void whenPostDoesNotExistDeleteInteractThenReturnNotFound() {
        UUID postIdFake = UUID.fromString("17722574-841b-40fd-9dcc-501bc9e99999");
        webTestClient.delete()
                .uri("/posts/{postId}/interacts", postIdFake)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(responseApi -> {
                    var message = JsonPath.parse(responseApi).read("$.message", String.class);
                    assertThat(message).isEqualTo("Could not find post with uuid: " + postIdFake);
                });
    }

    @Test
    void whenPostAlreadyExistButHaveNotExpressToItThenReturnBadRequest() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        webTestClient.delete()
                .uri("/posts/{postId}/interacts", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void whenPostAlreadyExistAndHaveExpressedToItThenReturnNoContent() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        Interact interact = Interact.of(Post.reference(postId), Interact.Emote.HAPPY);
        String responseApi = webTestClient.post()
                .uri("/interacts")
                .body(BodyInserters.fromValue(interact))
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        String msg = JsonPath.parse(responseApi).read("$.message", String.class);
        assertThat(msg).isEqualTo("Expressing your emotion to a post successfully.");
        UUID interactId = UUID.fromString(JsonPath.parse(responseApi).read("$.data.id", String.class));

        webTestClient.delete()
                .uri("/posts/{postId}/interacts", postId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri("/posts/{postId}/interacts/{interactId}", postId, interactId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenInteractAlreadyExistUpdateEmoteThenReturnOK() {
        UUID postId = UUID.fromString(JsonPath.parse(fakePost).read("$.data.id"));
        Interact interact = Interact.of(Post.reference(postId), Interact.Emote.HAPPY);
        String responseApi = webTestClient.post()
                .uri("/interacts")
                .body(BodyInserters.fromValue(interact))
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        var interactId = UUID.fromString(JsonPath.parse(responseApi).read("$.data.id", String.class));
        var interactUpdate = new Interact();
        interactUpdate.setEmote(Interact.Emote.SAD);
        webTestClient.put()
                .uri("/interacts/{interactId}", interactId)
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromValue(interactUpdate))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(result -> {
                    var message = JsonPath.parse(result).read("$.message", String.class);
                    assertThat(message).isEqualTo("Update emote for a interact successfully.");
                    var emote = JsonPath.parse(result).read("$.data.emote");
                    assertThat(emote).isEqualTo(Interact.Emote.SAD.toString());
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
