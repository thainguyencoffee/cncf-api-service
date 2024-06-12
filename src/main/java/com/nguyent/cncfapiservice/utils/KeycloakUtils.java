package com.nguyent.cncfapiservice.utils;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakUtils {

    @Value("${keycloak.auth-server-url}")
    private String SERVER_URL;

    @Value("${keycloak.realm}")
    private String REALM;

    @Value("${keycloak.resource}")
    private String CLIENT_ID;

    @Value("${keycloak.credentials.secret}")
    private String CREDENTIALS_SECRET;

    public Keycloak getKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .clientId(CLIENT_ID)
                .clientSecret(CREDENTIALS_SECRET)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }
}
