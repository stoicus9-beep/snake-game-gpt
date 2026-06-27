package com.stoicus9.snakeclassic.model

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    fun isOpposite(other: Direction): Boolean = when (this) {
        UP -> other == DOWN
        DOWN -> other == UP
        LEFT -> other == RIGHT
        RIGHT -> other == LEFT
    }
}
