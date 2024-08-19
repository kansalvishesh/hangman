# Hangman Assignment

## Overview

The application allows multiple players to join a game room, where they can guess letters to uncover a hidden word. The game includes features like generating unique game room names, tracking incorrect guesses, managing rounds, and assigning a word master(turn wise).

## Features

- **Unique Game Room Names**: Automatically generates unique 6-character room names for each game session.
- **Multi-Player Support**: Multiple players can join a game room and take turns guessing letters.
- **Word Master Role**: One player is designated as the word master, responsible for providing the word to guess.
- **Game State Management**: Tracks the current state of the game, including incorrect guesses, round count, and game status.
- **Kafka Integration**: Sends game state updates to Kafka topics for real-time processing.
- **Basic Authentication**: Secures the game room APIs using basic authentication with encoded credentials.

## Technology Stack

- **Java**: Core programming language.
- **Spring Boot**: Framework for building the application.
- **Spring Data JPA**: For database interactions.
- **Kafka**: For message brokering and real-time updates.
- **PostgreSQL**: Database for storing game data.

## Setup and Installation

### Prerequisites

- **Java 17**
- **Maven**
- **PostgreSQL**
- **Kafka**

### Database properties

- **spring.datasource.url**=jdbc:postgresql://localhost:5432/hangman_db
- **spring.datasource.username**=your_username
- **spring.datasource.password**=your_password
- **spring.jpa.hibernate.ddl-auto**=update

## API Endpoints

### User Endpoints

- **Register User**
  - **Method:** `POST`
  - **URL:** `/api/users/register`
  - **Request Body:**
    ```json
    {
        "username": "vishesh",
        "password": "test"
    }
    ```
  - **Response:**
      ```json
      {
        "id": 1,
        "username": "vishesh",
        "password": "dGVzdA=="
      }
      ```

- **Login User**
  - **Method:** `POST`
  - **URL:** `/api/users/login`
  - **Request Params:**
    - `username`: The username of the user.
    - `password`: The password of the user.
  - **Response:** `200 OK`
    - Returns the username and encoded credentials for authentication.
    ```json
    {
      "id": 3,
      "username": "garvit",
      "password": "dGVzdDI="
    }
    ```
  - **Error Response:** `401 Unauthorized`
    - Returns an error message if the username or password is incorrect.

### Game Room Endpoints

- **Start Game Room**
  - **Method:** `POST`
  - **URL:** `/api/gamerooms/start`
  - **Request Params:**
    - `wordMasterName`: The username of the user who will be the word master.
  - **Response:** `201 Created`
    - Returns the created `GameRoom` object with a unique room name.
    ```json
    {
    "id": 2,
    "roomName": "C9F623",
    "word": null,
    "currentState": null,
    "incorrectGuesses": 0,
    "gameStatus": "WAITING_FOR_WORD",
    "wordMaster": {
        "id": 2,
        "username": "ripu",
        "password": "dGVzdDE="
    },
    "roundCount": 0,
    "maxTries": 6
    }
    ```
  - **Error Response:** `500 Internal Server Error`
    - Returns an error message if the game room could not be created.

- **Join Game Room**
  - **Method:** `POST`
  - **URL:** `/api/gamerooms/join`
  - **Request Params:**
    - `gameRoomName`: The name of the game room to join.
    - `userName`: The username of the player joining the game room.
  - **Response:** `200 OK`
    - Returns the `PlayerGame` object representing the player in the game room.
    ```json
    {
        {
    "id": 2,
    "user": {
        "id": 1,
        "username": "vishesh",
        "password": "dGVzdA=="
    },
    "gameRoom": {
        "id": 2,
        "roomName": "C9F623",
        "word": null,
        "currentState": null,
        "incorrectGuesses": 0,
        "gameStatus": "WAITING_FOR_WORD",
        "wordMaster": {
            "id": 2,
            "username": "ripu",
            "password": "dGVzdDE="
        },
        "roundCount": 0,
        "maxTries": 6
    },
    "incorrectGuesses": 0,
    "score": 0,
    "hasBeenWordMaster": false,
    "wordMaster": false
      }
    }
  - **Note:** Will not allow to join the room if game is `IN_PROGRESS` 
    
  - **Error Response:** `404 Not Found`
    - Returns an error message if the game room is not found.
  - **Error Response:** `500 Internal Server Error`
    - Returns an error message if the player could not join the game room.

- **Set Word**
  - **Method:** `POST`
  - **URL:** `/api/gamerooms/{gameRoomName}/set-word`
  - **Path Variable:**
    - `gameRoomName`: The name of the game room.
  - **Request Params:**
    - `wordMasterName`: The username of the word master setting the word.
    - `word`: The word to be guessed in the game.
  - **Response:** `200 OK`
    - Returns the updated `GameRoom` object with the new word set.
    ```json
    {
    "id": 2,
    "roomName": "C9F623",
    "word": "television",
    "currentState": "__________",
    "incorrectGuesses": 0,
    "gameStatus": "IN_PROGRESS",
    "wordMaster": {
        "id": 2,
        "username": "ripu",
        "password": "dGVzdDE="
    },
    "roundCount": 0,
    "maxTries": 6
    }
    ```
  - **Error Response:** `500 Internal Server Error`
    - Returns an error message if the word could not be set.

- **Make a Guess**
  - **Method:** `POST`
  - **URL:** `/api/gamerooms/{gameRoomName}/guess`
  - **Path Variable:**
    - `gameRoomName`: The name of the game room.
  - **Request Params:**
    - `userName`: vishesh.
    - `letter`: l.
  - **Response:** `200 OK`
    - Returns the updated `GameRoom` object after the guess is processed.
    ```json
     {
    "id": 2,
    "roomName": "C9F623",
    "word": "television",
    "currentState": "__l_______",
    "incorrectGuesses": 0,
    "gameStatus": "IN_PROGRESS",
    "wordMaster": {
        "id": 2,
        "username": "ripu",
        "password": "dGVzdDE="
    },
    "roundCount": 0,
    "maxTries": 6
    }
    ```
  - **Error Response:** `500 Internal Server Error`
    - Returns an error message if the guess could not be processed.
  - **Scoring Mechanism:** If a single letter is correctly guessed then 1 point is given. If the whole word is correctly guessed then 3 points are given.
  - **Turn wise word master:** Once the round has been completed(either incorrect guess >= max tries or word has been correctly guessed), the word master is changed to the other user in the room. This happens till all users in the room have had a chance to be the word master. 

### Kafka Integration

- **Game State Updates**
  - Game state updates are sent to Kafka topics for real-time processing. (Topic name: game-room).
- **Steps to watch real time updates**
  - Download Apache Kafka and cd to the `kafka_2.13-3.2.0`
  - Run `bin/zookeeper-server-start.sh config/zookeeper.properties`.
  - Run `bin/kafka-server-start.sh config/server.properties`.
  - Run `bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic game-room --from-beginning`
  - **Game Stats** (with new word master in case of word master change)
  ```json
   {
    "playerScores": {
        "vishesh": 1,
        "garvit": 3,
        "ripu": 0
    },
    "chancesLeft": 6,
    "currentRound": 2,
    "totalRounds": 3,
    "wordMaster": {
        "id": 3,
        "username": "garvit",
        "password": "dGVzdDI="
    },
    "currentWord": "television",
    "wordToGuess": "television",
    "gameStatus": "ROUND_COMPLETED",
    "gameRoomId": 2,
    "gameRoomName": "C9F623"
    }
    ```
### Authentication

- **Basic Authentication** is required for accessing protected endpoints.
  - After logging in, use the encoded credentials returned by the `/api/users/login` endpoint in the `Authorization` header for subsequent requests.
  - **Example Authorization Header:**
    ```
    Authorization: Basic dGVzdHVzZXI6cGFzc3dvcmQxMjM=
    ```
  

