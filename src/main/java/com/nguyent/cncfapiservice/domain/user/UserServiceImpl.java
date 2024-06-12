package com.nguyent.cncfapiservice.domain.user;

import com.nguyent.cncfapiservice.dto.UserUpdateDto;
import com.nguyent.cncfapiservice.utils.KeycloakUtils;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final KeycloakUtils keycloakUtils;
    private static final String REALM = "chat";

    public UserServiceImpl(KeycloakUtils keycloakUtils) {
        this.keycloakUtils = keycloakUtils;
    }

    @Override
    public List<UserRepresentation> getAllUser(Pageable pageable) {
        log.info("UserServiceImpl.getAllUser with pageable offset {} and pageSize {}",
                pageable.getOffset(), pageable.getPageSize());

        try (var keycloak = keycloakUtils.getKeycloak()) {
            return keycloak.realm(REALM).users()
                    .list((int) pageable.getOffset(), pageable.getPageSize());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public UserRepresentation getUserById(String id) {
        log.info("UserServiceImpl.getUserById with id {}", id);
        try (var keycloak = keycloakUtils.getKeycloak()) {
            var userResource = keycloak.realm(REALM).users().get(id);
            return userResource.toRepresentation();
        } catch (NotFoundException ex) {
            throw new UserRepresentationNotFoundException(id);
        }
    }

    @Override
    public List<UserRepresentation> searchUserByUsername(String username, Pageable pageable) {
        log.info("UserServiceImpl.searchUserByUsername with " +
                "username {} and with pageable offset {} and pageSize {}",
                username, pageable.getOffset(), pageable.getPageSize());

        try (var keycloak = keycloakUtils.getKeycloak()) {
            var userRepresentations = keycloak.realm(REALM).users().searchByUsername(username, false);
            if (userRepresentations.isEmpty()) {
                throw new UserRepresentationNotFoundException(username);
            }
            return userRepresentations.subList((int)pageable.getOffset(), Math.min(userRepresentations.size(), pageable.getPageSize()));
        }  catch (NotFoundException ex) {
            throw new UserRepresentationNotFoundException(username);
        }
    }

    @Override
    public UserRepresentation updateUserById(String userId, UserUpdateDto userUpdateDto) {
        log.info("UserServiceImpl.updateUserById with id {}", userId);
        try (var keycloak = keycloakUtils.getKeycloak()) {
            var userResource = keycloak.realm(REALM).users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            user.setFirstName(Optional.ofNullable(userUpdateDto.firstName())
                    .orElseGet(user::getFirstName));
            user.setLastName(Optional.ofNullable(userUpdateDto.lastName())
                    .orElseGet(user::getLastName));
            user.setEmail(Optional.ofNullable(userUpdateDto.email())
                    .orElseGet(user::getEmail));
            user.setAttributes(Map.of(
                    "birthdate", List.of(Optional.ofNullable(userUpdateDto.birthdate())
                            .orElseGet(() -> user.firstAttribute("birthdate"))),
                    "gender", List.of(Optional.ofNullable(userUpdateDto.gender())
                            .orElseGet(() -> user.firstAttribute("gender")))
            ));
            userResource.update(user);
            return keycloak.realm(REALM).users().get(userId).toRepresentation();
        } catch (NotFoundException ex) {
            throw new UserRepresentationNotFoundException(userId);
        }
    }
}
