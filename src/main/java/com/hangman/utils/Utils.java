package com.hangman.utils;

public class Utils {
    public static final String DICT_API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public static final Integer MAX_SCORE = 3;
    public static final Integer MAX_TRIES = 6;
    public static final Integer SCORE_FOR_SINGLE_LETTER = 1;

    public enum GameStatus {
        WAITING_FOR_WORD,
        IN_PROGRESS,
        ROUND_COMPLETED,
        ALL_ROUNDS_COMPLETED,
    }

}
