package com.hangman.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hangman.entities.GameRoom;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Optional<GameRoom> findByRoomName(String roomName);
}
