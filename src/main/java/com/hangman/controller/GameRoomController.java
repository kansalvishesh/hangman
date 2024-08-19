package com.hangman.controller;

import com.hangman.entities.GameRoom;
import com.hangman.entities.PlayerGame;
import com.hangman.service.GameRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/gamerooms")
public class GameRoomController {

    @Autowired
    private GameRoomService gameRoomService;

    @PostMapping("/start")
    public ResponseEntity<?> startGameRoom(@RequestParam String wordMasterName) {
        try {
            GameRoom gameRoom = gameRoomService.startGameRoom(wordMasterName);
            return new ResponseEntity<>(gameRoom, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to start game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinGameRoom(@RequestParam String gameRoomName, @RequestParam String userName) {
        try {
            Optional<PlayerGame> playerGame = gameRoomService.joinGameRoom(gameRoomName, userName);
            return playerGame.map(ResponseEntity::ok)
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to join game room: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{gameRoomName}/set-word")
    public ResponseEntity<?> setWord(@PathVariable String gameRoomName,
                                     @RequestParam String wordMasterName,
                                     @RequestParam String word) {
        try {
            GameRoom gameRoom = gameRoomService.setWord(gameRoomName, wordMasterName, word);
            return new ResponseEntity<>(gameRoom, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to set word: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{gameRoomName}/guess")
    public ResponseEntity<?> makeGuess(@PathVariable String gameRoomName,
                                       @RequestParam String userName,
                                       @RequestParam String letter) {
        try {
            GameRoom gameRoom = gameRoomService.makeGuess(gameRoomName, userName, letter);
            return new ResponseEntity<>(gameRoom, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to make a guess: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
