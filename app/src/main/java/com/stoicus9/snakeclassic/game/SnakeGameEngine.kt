package com.stoicus9.snakeclassic.game

import com.stoicus9.snakeclassic.model.Direction
import com.stoicus9.snakeclassic.model.GameState
import com.stoicus9.snakeclassic.model.GameStatus
import com.stoicus9.snakeclassic.model.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.random.Random

class SnakeGameEngine(
    private val columns: Int = DEFAULT_COLUMNS,
    private val rows: Int = DEFAULT_ROWS,
    initialHighScore: Int = 0,
    private val random: Random = Random.Default
) {
    private val _state = MutableStateFlow(createInitialState(initialHighScore))
    val state: StateFlow<GameState> = _state.asStateFlow()

    fun start() {
        val current = _state.value
        _state.value = when (current.status) {
            GameStatus.READY -> current.copy(status = GameStatus.RUNNING)
            GameStatus.GAME_OVER -> createInitialState(current.highScore).copy(status = GameStatus.RUNNING)
            else -> current
        }
    }

    fun pause() {
        val current = _state.value
        if (current.status == GameStatus.RUNNING) {
            _state.value = current.copy(status = GameStatus.PAUSED)
        }
    }

    fun resume() {
        val current = _state.value
        if (current.status == GameStatus.PAUSED) {
            _state.value = current.copy(status = GameStatus.RUNNING)
        }
    }

    fun restart() {
        _state.value = createInitialState(_state.value.highScore)
    }

    fun changeDirection(newDirection: Direction) {
        val current = _state.value
        if (current.status != GameStatus.RUNNING) return
        if (newDirection == current.direction) return
        if (newDirection.isOpposite(current.direction)) return
        _state.value = current.copy(nextDirection = newDirection)
    }

    fun tick(): TickResult {
        val current = _state.value
        if (current.status != GameStatus.RUNNING || current.snake.isEmpty()) return TickResult.None

        val direction = current.nextDirection
        val currentHead = current.snake.first()
        val newHead = currentHead.move(direction)

        if (isWallCollision(newHead)) {
            _state.value = current.copy(status = GameStatus.GAME_OVER, direction = direction)
            return TickResult.GameOver
        }

        val willEatFood = newHead == current.food
        val bodyForCollisionCheck = if (willEatFood) current.snake else current.snake.dropLast(1)

        if (newHead in bodyForCollisionCheck) {
            _state.value = current.copy(status = GameStatus.GAME_OVER, direction = direction)
            return TickResult.GameOver
        }

        val newSnake = if (willEatFood) {
            listOf(newHead) + current.snake
        } else {
            listOf(newHead) + current.snake.dropLast(1)
        }

        if (!willEatFood) {
            _state.value = current.copy(
                snake = newSnake,
                direction = direction,
                nextDirection = direction
            )
            return TickResult.None
        }

        val newScore = current.score + SCORE_PER_FOOD
        val newHighScore = max(current.highScore, newScore)
        val newFood = spawnFood(newSnake)
        val boardFilled = newFood == null

        _state.value = current.copy(
            snake = newSnake,
            food = newFood ?: current.food,
            direction = direction,
            nextDirection = direction,
            score = newScore,
            highScore = newHighScore,
            status = if (boardFilled) GameStatus.GAME_OVER else GameStatus.RUNNING,
            tickDelayMillis = calculateTickDelay(newScore)
        )

        return TickResult(
            ateFood = true,
            gameOver = boardFilled,
            highScoreChanged = newHighScore > current.highScore
        )
    }

    private fun createInitialState(highScore: Int): GameState {
        val centerRow = rows / 2
        val centerCol = columns / 2
        val snake = listOf(
            Position(centerRow, centerCol),
            Position(centerRow, centerCol - 1),
            Position(centerRow, centerCol - 2)
        )

        return GameState(
            snake = snake,
            food = spawnFood(snake) ?: Position(centerRow, (centerCol + 4).coerceAtMost(columns - 1)),
            direction = Direction.RIGHT,
            nextDirection = Direction.RIGHT,
            score = 0,
            highScore = highScore,
            status = GameStatus.READY,
            tickDelayMillis = INITIAL_TICK_DELAY_MS,
            columns = columns,
            rows = rows
        )
    }

    private fun isWallCollision(position: Position): Boolean {
        return position.row !in 0 until rows || position.col !in 0 until columns
    }

    private fun spawnFood(snake: List<Position>): Position? {
        val occupied = snake.toHashSet()
        val emptyCells = ArrayList<Position>(columns * rows - occupied.size)

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val candidate = Position(row, col)
                if (candidate !in occupied) {
                    emptyCells.add(candidate)
                }
            }
        }

        if (emptyCells.isEmpty()) return null
        return emptyCells[random.nextInt(emptyCells.size)]
    }

    private fun calculateTickDelay(score: Int): Int {
        val speedSteps = score / SPEED_UP_EVERY_POINTS
        return (INITIAL_TICK_DELAY_MS - speedSteps * SPEED_STEP_MS).coerceAtLeast(MIN_TICK_DELAY_MS)
    }

    companion object {
        const val DEFAULT_COLUMNS = 20
        const val DEFAULT_ROWS = 30
        private const val INITIAL_TICK_DELAY_MS = 180
        private const val MIN_TICK_DELAY_MS = 80
        private const val SPEED_UP_EVERY_POINTS = 50
        private const val SPEED_STEP_MS = 10
        private const val SCORE_PER_FOOD = 10
    }
}

data class TickResult(
    val ateFood: Boolean = false,
    val gameOver: Boolean = false,
    val highScoreChanged: Boolean = false
) {
    companion object {
        val None = TickResult()
        val GameOver = TickResult(gameOver = true)
    }
}
