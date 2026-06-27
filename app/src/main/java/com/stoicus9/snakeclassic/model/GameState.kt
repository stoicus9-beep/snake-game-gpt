package com.stoicus9.snakeclassic.model

data class GameState(
    val snake: List<Position>,
    val food: Position,
    val direction: Direction,
    val nextDirection: Direction,
    val score: Int,
    val highScore: Int,
    val status: GameStatus,
    val tickDelayMillis: Int,
    val columns: Int,
    val rows: Int
)
