package com.nguyent.cncfapiservice.web;

import com.nguyent.cncfapiservice.domain.user.UserService;
import com.nguyent.cncfapiservice.dto.UserUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(produces = "application/json")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

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

    @PutMapping(value = "/users", consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseApi updateUser(@AuthenticationPrincipal Jwt jwt, @RequestBody UserUpdateDto userUpdateDto) {
        log.info("UserController updateUser");
        return new ResponseApi("OK", 200, "User updated successfully.",
                null, userService.updateUserById(jwt.getSubject(), userUpdateDto));
    }

}
