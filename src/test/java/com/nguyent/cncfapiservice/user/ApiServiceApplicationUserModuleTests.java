package com.nguyent.cncfapiservice.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.nguyent.cncfapiservice.config.DataConfig;
import com.nguyent.cncfapiservice.domain.post.Post;
import com.nguyent.cncfapiservice.dto.UserUpdateDto;
import com.nguyent.cncfapiservice.web.ResponseApi;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DataConfig.class)
@Testcontainers
class ApiServiceApplicationUserModuleTests {

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
    void whenUnauthenticatedGetUsersThenOK() {
        webTestClient.get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("username", "user").build()
                )
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/users/{userId}", 123)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenUserExistsDoGetUserByUserIdThenReturn() {
        String userId = "4a069652-5a37-4286-9da5-6248a734a989";
        String message = "Get user by id successfully.";
        webTestClient.get()
                .uri("/users/{userId}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext documentContext = JsonPath.parse(responseApi);
                    assertThat(documentContext.read("$.message", String.class)).isEqualTo(message);
                    assertThat(documentContext.read("$.data.id", String.class)).isEqualTo(userId);
                });
    }

    @Test
    void whenUsernameExistsDoGetUserByUsernameThenOK() {
        var username = "user";
        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("username", username)
                        .queryParam("page", 0)
                        .queryParam("size", 10).build()
                )
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext jsonContext = JsonPath.parse(responseApi);
                    assertThat(jsonContext.read("$.data[0].username", String.class)).isEqualTo(username);
                });
    }

    @Test
    void whenUsernameNotExistsDoGetUserByUsernameThenNotFound() {
        var username = "username-not-exists";
        webTestClient.get().uri("/users/search?username=" + username)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ResponseApi.class)
                .value(responseError -> {
                    assertThat(responseError.getStatusNumber()).isEqualTo(404);
                    assertThat(responseError.getData()).isNull();
                });
    }

    @Test
    void whenUnauthenticatedPutUsersThenUnauthorized() {
        webTestClient.put().uri("/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void whenAuthenticatedPutUsersThenOK() {
        var userUpdateDto = new UserUpdateDto("firstName update",
                "lastName update",
                "email@gmail.com", "10/10/2004", "Female", null);
        MultiValueMap<String, String> multiMap = new LinkedMultiValueMap<>();
        multiMap.put("firstName", List.of(userUpdateDto.firstName()));
        multiMap.put("lastName", List.of(userUpdateDto.lastName()));
        multiMap.put("email", List.of(userUpdateDto.email()));
        multiMap.put("birthdate", List.of(userUpdateDto.birthdate()));
        webTestClient.put().uri("/users")
                .headers(headers -> headers.setBearerAuth(userToken.accessToken()))
                .body(BodyInserters.fromMultipartData(multiMap))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(responseApi -> {
                    DocumentContext jsonContext = JsonPath.parse(responseApi);
                    assertThat(jsonContext.read("$.message", String.class)).isEqualTo("User updated successfully.");
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