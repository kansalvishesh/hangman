package com.hangman.controller;

import com.hangman.entities.User;
import com.hangman.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hangman.repositories.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User createdUser = userService.signup(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestParam String username, @RequestParam String password) {
        Optional<User> user = userService.login(username, password);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }
}
