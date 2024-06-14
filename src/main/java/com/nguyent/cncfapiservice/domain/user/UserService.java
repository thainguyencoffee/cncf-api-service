package com.nguyent.cncfapiservice.domain.user;

import com.nguyent.cncfapiservice.dto.UserUpdateDto;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserRepresentation> getAllUser(Pageable pageable);

    UserRepresentation getUserById(String id);

    List<UserRepresentation> searchUserByUsername(String username, Pageable pageable);

    UserRepresentation updateUserById(String userId, UserUpdateDto userUpdateDto);

    void sendVerificationEmail(UUID userId);
}
