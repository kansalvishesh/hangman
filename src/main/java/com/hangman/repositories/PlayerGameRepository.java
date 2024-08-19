package com.hangman.repositories;

import com.hangman.entities.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {
    Optional<PlayerGame> findByUserIdAndGameRoomId(Long userId, Long gameRoomId);
    List<PlayerGame> findByGameRoomId(Long gameRoomId);
    Integer countByGameRoomId(Long gameRoomId);
}