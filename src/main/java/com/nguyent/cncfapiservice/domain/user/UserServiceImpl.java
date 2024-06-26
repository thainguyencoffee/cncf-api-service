package com.nguyent.cncfapiservice.domain.user;

import com.cloudinary.Cloudinary;
import com.nguyent.cncfapiservice.cloudinary.CloudinaryUtils;
import com.nguyent.cncfapiservice.dto.UserUpdateDto;
import com.nguyent.cncfapiservice.utils.KeycloakUtils;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KeycloakUtils keycloakUtils;
    private static final String REALM = "chat";
    private final Cloudinary cloudinary;

    @Override
    public List<UserRepresentation> getAllUser(Pageable pageable) {
        log.info("UserServiceImpl.getAllUser with pageable offset {} and pageSize {}",
                pageable.getOffset(), pageable.getPageSize());

        try (var keycloak = keycloakUtils.getKeycloak()) {
            // Lấy danh sách người dùng từ Keycloak
            List<UserRepresentation> users = keycloak.realm(REALM).users()
                    .list((int) pageable.getOffset(), pageable.getPageSize());
            // Duyệt qua từng người dùng để lấy vai trò của họ
            for (UserRepresentation user : users) {
                List<RoleRepresentation> roles = getUserRoles(keycloak, user.getId());
                // Giả định rằng UserRepresentation có phương thức setRealmRoles hoặc tương tự
                user.setRealmRoles(roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList()));
            }
            return users;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<RoleRepresentation> getUserRoles(Keycloak keycloak, String userId) {
        UserResource userResource = keycloak.realm(REALM).users().get(userId);
        return userResource.roles().realmLevel().listEffective();
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
            return userRepresentations.subList((int) pageable.getOffset(), Math.min(userRepresentations.size(), pageable.getPageSize()));
        } catch (NotFoundException ex) {
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

            // Optional handle picture
            String picture = Optional.ofNullable(userUpdateDto.picture())
                    .map(pictureMultiPart -> {
                        try {
                            // handle delete old picture
                            String oldPicture = user.firstAttribute("picture");
                            if (oldPicture != null) {
                                var publicId = CloudinaryUtils.convertUrlToPublicId(oldPicture);
                                log.info("oldPicture {}", oldPicture);
                                log.info("publicId {}", publicId);
                                String status = CloudinaryUtils.deleteFile(publicId, cloudinary);
                                log.info("deleteFile {}", status);
                            }
                            // upload new picture
                            return CloudinaryUtils.convertSingleMultipartFileToUrl(pictureMultiPart, cloudinary);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }).orElseGet(() -> user.firstAttribute("picture"));
            user.setAttributes(Map.of(
                    "birthdate", List.of(Optional.ofNullable(userUpdateDto.birthdate())
                            .orElseGet(() -> user.firstAttribute("birthdate"))),
                    "gender", List.of(Optional.ofNullable(userUpdateDto.gender())
                            .orElseGet(() -> user.firstAttribute("gender"))),
                    "picture", List.of(picture != null ? picture : "")
            ));
            userResource.update(user);
            return keycloak.realm(REALM).users().get(userId).toRepresentation();
        } catch (NotFoundException ex) {
            throw new UserRepresentationNotFoundException(userId);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendVerificationEmail(UUID userId) {
        log.info("UserServiceImpl.sendVerificationEmail with id {}", userId);
        try (var keycloak = keycloakUtils.getKeycloak()) {
            keycloak.realm(REALM).users().get(userId.toString()).executeActionsEmail(List.of("VERIFY_EMAIL"));
        } catch (NotFoundException ex) {
            throw new UserRepresentationNotFoundException(userId.toString());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send verification email to user with id " + userId, ex);
        }
    }
}
