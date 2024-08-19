package com.hangman.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PlayerGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_room_id", nullable = false)
    private GameRoom gameRoom;

    private boolean isWordMaster;
    private int incorrectGuesses;
    private int score;
    private boolean hasBeenWordMaster;

}
