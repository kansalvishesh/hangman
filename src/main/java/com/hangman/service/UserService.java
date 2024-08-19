package com.hangman.service;

import com.hangman.entities.User;
import com.hangman.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User signup(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User newUser = new User();
        newUser.setUsername(user.getUsername());

        // Encode the password using Base64
        String encodedPassword = Base64.getEncoder().encodeToString(user.getPassword().getBytes());
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            // Decode the stored password and compare it with the provided password
            String decodedPassword = new String(Base64.getDecoder().decode(user.get().getPassword()));
            if (password.equals(decodedPassword)) {
                return user;
            }
        }
        return Optional.empty();
    }
}
