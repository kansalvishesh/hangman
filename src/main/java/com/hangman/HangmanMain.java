package com.hangman;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.hangman.repositories")
public class HangmanMain {
    public static void main(String[] args) {
        try {
            SpringApplication.run(HangmanMain.class, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

//test cases:
//user already present.
//no auth headers sent.
//trying to send api without login.
//
