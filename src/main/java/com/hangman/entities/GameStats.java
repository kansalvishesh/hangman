package com.hangman.entities;

import com.hangman.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;

@Getter
@Setter
@ToString
public class GameStats {
    private HashMap<String, Integer> playerScores;
    private Integer chancesLeft;
    private Integer currentRound;
    private Integer totalRounds;
    private User wordMaster;
    private String currentWord;
    private String wordToGuess;
    private Utils.GameStatus gameStatus;
    private Long gameRoomId;
    private String gameRoomName;
}
