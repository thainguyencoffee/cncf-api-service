package com.nguyent.cncfapiservice.web;

import com.nguyent.cncfapiservice.domain.user.UserService;
import com.nguyent.cncfapiservice.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(produces = "application/json")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi getAllUser(Pageable pageable) {
        log.info("UserController getAllUser");
        return new ResponseApi("OK", 200, "Get all users successfully.",
                null, userService.getAllUser(pageable));
    }

    @GetMapping("/users/{id}")
    public ResponseApi getUserById(@PathVariable("id") String id) {
        log.info("UserController getUserById");
        return new ResponseApi("OK", 200, "Get user by id successfully.",
                null, userService.getUserById(id));
    }

    @GetMapping(value = "/users/search", produces = "application/json")
    public ResponseApi searchByUsername(@RequestParam String username, Pageable pageable) {
        log.info("UserController searchByUsername");
        return new ResponseApi("OK", 200, "Search users by username successfully.",
                null, userService.searchUserByUsername(username, pageable));
    }

    @PutMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi updateUser(@AuthenticationPrincipal Jwt jwt, @ModelAttribute UserUpdateDto userUpdateDto) {
        log.info("UserController updateUser");
        return new ResponseApi("OK", 200, "User updated successfully.",
                null, userService.updateUserById(jwt.getSubject(), userUpdateDto));
    }

    @PostMapping("/users/send-verification-email")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi sendVerificationEmail(@AuthenticationPrincipal Jwt jwt) {
        log.info("UserController sendVerificationEmail");
        userService.sendVerificationEmail(UUID.fromString(jwt.getSubject()));
        return new ResponseApi("OK", 200, "Send verification email successfully.",
                null, null);
    }

}
