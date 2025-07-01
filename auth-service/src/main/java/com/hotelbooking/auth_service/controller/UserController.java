package com.hotelbooking.auth_service.controller;

import com.hotelbooking.auth_service.respositoy.UserRepository;
import com.hotelbooking.common_model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> showUser(@PathVariable String id) {
        log.info("Fetching user with id {}", id);
        Long userId = Long.parseLong(id.trim());
        Optional<User> fetchedUser = userRepository.findById(userId);

        if (fetchedUser.isEmpty()) {
            log.info("User with id {} not found", id);
            return ResponseEntity.notFound().build();
        }

        User user = fetchedUser.get();

        user.setPassword(null);
        user.setRole(null);

        return ResponseEntity.ok(user);
    }
}
