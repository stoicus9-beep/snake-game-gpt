package com.stoicus9.snakeclassic.model

data class Position(
    val row: Int,
    val col: Int
) {
    fun move(direction: Direction): Position = when (direction) {
        Direction.UP -> copy(row = row - 1)
        Direction.DOWN -> copy(row = row + 1)
        Direction.LEFT -> copy(col = col - 1)
        Direction.RIGHT -> copy(col = col + 1)
    }
}
