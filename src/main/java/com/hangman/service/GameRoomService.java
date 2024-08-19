package com.hangman.service;

import com.hangman.utils.Utils;
import com.hangman.apiManager.DictionaryApiManager;
import com.hangman.entities.GameRoom;
import com.hangman.entities.GameStats;
import com.hangman.entities.PlayerGame;
import com.hangman.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.hangman.repositories.GameRoomRepository;
import com.hangman.repositories.PlayerGameRepository;
import com.hangman.repositories.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.hangman.utils.Utils.*;

@Service
public class GameRoomService {

    @Autowired
    private GameRoomRepository gameRoomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlayerGameRepository playerGameRepository;
    @Autowired
    private DictionaryApiManager dictionaryApiManager;
    private static final String TOPIC = "game-room";

    @Autowired
    private KafkaTemplate<String, GameStats> kafkaTemplateWithStringData;

    public GameRoom startGameRoom(String wordMasterName) {
        User wordMaster = userRepository.findByUsername(wordMasterName)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String roomName = generateRandomRoomName();

        GameRoom gameRoom = new GameRoom();
        gameRoom.setRoomName(roomName);
        gameRoom.setWordMaster(wordMaster);
        gameRoom.setMaxTries(MAX_TRIES);
        gameRoom.setGameStatus(Utils.GameStatus.WAITING_FOR_WORD);

        GameRoom savedGameRoom = gameRoomRepository.save(gameRoom);

        PlayerGame playerGame = new PlayerGame();
        playerGame.setGameRoom(savedGameRoom);
        playerGame.setUser(wordMaster);
        playerGame.setWordMaster(true);
        playerGame.setHasBeenWordMaster(true);

        playerGameRepository.save(playerGame);

        return savedGameRoom;
    }

    public Optional<PlayerGame> joinGameRoom(String roomName, String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GameRoom gameRoom = gameRoomRepository.findByRoomName(roomName)
                .orElseThrow(() -> new RuntimeException("Game room not found"));
        Optional<PlayerGame> playerGameOpt = playerGameRepository.findByUserIdAndGameRoomId(user.getId(), gameRoom.getId());
        if (playerGameOpt.isPresent()) {
            throw new IllegalArgumentException("User already present in game room!");
        }
        if (!gameRoom.getGameStatus().equals(Utils.GameStatus.WAITING_FOR_WORD)) { //reverse it
            throw new RuntimeException("Game has already started!");
        }
        PlayerGame playerGame = new PlayerGame();
        playerGame.setGameRoom(gameRoom);
        playerGame.setUser(user);
        return Optional.of(playerGameRepository.save(playerGame));

    }

    public GameRoom setWord(String gameRoomName, String wordMasterName, String word) {
        GameRoom gameRoom = gameRoomRepository.findByRoomName(gameRoomName)
                .orElseThrow(() -> new RuntimeException("GameRoom not found"));

        List<PlayerGame> playerGames = playerGameRepository.findByGameRoomId(gameRoom.getId());
        if (playerGames.size() < 2) {
            throw new RuntimeException("Need two or more players to start the game!");
        }

        PlayerGame wordMaster = playerGames.stream()
                .filter(player -> !player.isHasBeenWordMaster())
                .findFirst()
                .orElse(null);

        if (wordMaster == null) {
            throw new IllegalArgumentException("This game has been completed. Please create a new game room!");
        }

        if (!gameRoom.getWordMaster().getUsername().equals(wordMasterName)) {
            throw new RuntimeException("Only the Word Master can set the word.");
        }

        dictionaryApiManager.validateWord(word);

        gameRoom.setWord(word);
        gameRoom.setCurrentState("_".repeat(word.length()));
        gameRoom.setIncorrectGuesses(0);
        gameRoom.setGameStatus(Utils.GameStatus.IN_PROGRESS);

        kafkaTemplateWithStringData.send(TOPIC, getGameStats(gameRoom));

        return gameRoomRepository.save(gameRoom);
    }

    public GameRoom makeGuess(String gameRoomName, String userName, String letter) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new RuntimeException("User not found"));
        GameRoom gameRoom = gameRoomRepository.findByRoomName(gameRoomName)
                .orElseThrow(() -> new RuntimeException("GameRoom not found"));

        if (gameRoom.getGameStatus().equals(Utils.GameStatus.ALL_ROUNDS_COMPLETED)) {
            throw new RuntimeException("All rounds of this game have been completed. Please start a new game!");
        }

        if (gameRoom.getWordMaster().getUsername().equals(userName)) {
            throw new RuntimeException("Word Master cannot make guesses.");
        }

        PlayerGame playerGame = playerGameRepository.findByUserIdAndGameRoomId(user.getId(), gameRoom.getId())
                .orElseThrow(() -> new RuntimeException("GameRoom not found"));


        String word = gameRoom.getWord();
        String currentState = gameRoom.getCurrentState();
        StringBuilder updatedState = new StringBuilder(currentState);

        boolean correctGuess = false;
        if (letter.length() == 1) {
            if (updatedState.toString().contains(letter)) {
                throw new IllegalArgumentException("Letter %s Already guessed".formatted(letter));
            }
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == letter.charAt(0)) {
                    updatedState.setCharAt(i, letter.charAt(0));
                    correctGuess = true;
                }
            }
            if (correctGuess) {
                //single point for a single letter
                playerGame.setScore(playerGame.getScore() + SCORE_FOR_SINGLE_LETTER);
            }
        } else {
            if (letter.equalsIgnoreCase(word)) {
                //3 points for complete word
                updatedState.replace(0, updatedState.length(), letter);
                correctGuess = true;
                playerGame.setScore(playerGame.getScore() + MAX_SCORE);
            }
        }

        if (!correctGuess) {
            gameRoom.setIncorrectGuesses(gameRoom.getIncorrectGuesses() + 1);
            playerGame.setIncorrectGuesses(playerGame.getIncorrectGuesses() + 1);
        }
        completeRoundIfNecessary(gameRoom, updatedState.toString());
        gameRoom.setCurrentState(updatedState.toString());

        playerGameRepository.save(playerGame);
        gameRoomRepository.save(gameRoom);

        kafkaTemplateWithStringData.send(TOPIC, getGameStats(gameRoom));

        return gameRoom;
    }

    private GameStats getGameStats(GameRoom gameRoom) {
        GameStats gameStats = new GameStats();

        gameStats.setWordMaster(gameRoom.getWordMaster());
        gameStats.setCurrentWord(gameRoom.getCurrentState());
        gameStats.setWordToGuess(gameRoom.getWord());

        gameStats.setGameStatus(gameRoom.getGameStatus());

        gameStats.setChancesLeft(gameRoom.getMaxTries() - gameRoom.getIncorrectGuesses());

        gameStats.setCurrentRound(gameRoom.getRoundCount() + 1);
        gameStats.setTotalRounds(playerGameRepository.countByGameRoomId(gameRoom.getId()));

        List<PlayerGame> playerGames = playerGameRepository.findByGameRoomId(gameRoom.getId());
        HashMap<String, Integer> playerScores = new HashMap<>();
        for (PlayerGame playerGame : playerGames) {
            playerScores.put(playerGame.getUser().getUsername(), playerGame.getScore());
        }
        gameStats.setPlayerScores(playerScores);
        gameStats.setGameRoomId(gameRoom.getId());
        gameStats.setGameRoomName(gameRoom.getRoomName());

        return gameStats;
    }

    private void completeRoundIfNecessary(GameRoom gameRoom, String updatedState) {
        if (gameRoom.getIncorrectGuesses() >= gameRoom.getMaxTries() || !updatedState.contains("_")) {

            gameRoom.setGameStatus(Utils.GameStatus.ROUND_COMPLETED);

            rotateWordMaster(gameRoom);

            gameRoom.setRoundCount(gameRoom.getRoundCount() + 1);
            gameRoom.setIncorrectGuesses(0);
            gameRoom.setMaxTries(MAX_TRIES);

            int playerCount = playerGameRepository.findByGameRoomId(gameRoom.getId()).size();
            if (gameRoom.getRoundCount() >= playerCount) {
                gameRoom.setGameStatus(Utils.GameStatus.ALL_ROUNDS_COMPLETED);
            }
        }
    }

    private void rotateWordMaster(GameRoom gameRoom) {

        List<PlayerGame> players = playerGameRepository.findByGameRoomId(gameRoom.getId());

        PlayerGame currentWordMaster = players.stream()
                .filter(PlayerGame::isWordMaster)
                .findFirst()
                .orElse(null);

        PlayerGame newWordMaster = players.stream()
                .filter(player -> !player.isHasBeenWordMaster())
                .findFirst()
                .orElse(null);

        // Clear the old word master
        if (currentWordMaster != null) {
            currentWordMaster.setWordMaster(false);
            playerGameRepository.save(currentWordMaster);
        }
        if (newWordMaster != null) {
            newWordMaster.setWordMaster(true);
            newWordMaster.setHasBeenWordMaster(true);
        } else {
            return;
        }

        playerGameRepository.save(newWordMaster);
        gameRoom.setWordMaster(newWordMaster.getUser());
    }

    private String generateRandomRoomName() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

}
