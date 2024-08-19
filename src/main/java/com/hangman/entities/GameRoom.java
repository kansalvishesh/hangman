package com.hangman.entities;


import com.hangman.utils.Utils.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
public class GameRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;
    private String word;
    private String currentState;
    @Column(columnDefinition = "int default 0")
    private int incorrectGuesses;

    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;
    @ManyToOne
    @JoinColumn(name = "word_master_id")
    private User wordMaster;

    @Column(columnDefinition = "int default 0")
    private int roundCount;

    private int maxTries;
}
