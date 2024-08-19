package com.hangman;

import com.hangman.entities.User;
import com.hangman.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Base64;
import java.util.Optional;

@Component
public class BasicAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            // Extract credentials
            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] values = credentials.split(":", 2);

            String username = values[0];
            String password = values[1];

            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String decodedPassword = new String(Base64.getDecoder().decode(user.getPassword()));
                if (password.equals(decodedPassword)) {
                    return true;
                }
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized");
        return false;
    }
}
