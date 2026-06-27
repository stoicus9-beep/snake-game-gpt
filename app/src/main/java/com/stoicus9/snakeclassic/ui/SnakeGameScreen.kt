package com.stoicus9.snakeclassic.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stoicus9.snakeclassic.game.SnakeGameEngine
import com.stoicus9.snakeclassic.model.Direction
import com.stoicus9.snakeclassic.model.GameState
import com.stoicus9.snakeclassic.model.GameStatus
import com.stoicus9.snakeclassic.model.Position
import com.stoicus9.snakeclassic.ui.theme.ArcadeGreen
import com.stoicus9.snakeclassic.ui.theme.ArcadeGreenDark
import com.stoicus9.snakeclassic.ui.theme.ArcadeOrange
import com.stoicus9.snakeclassic.ui.theme.BoardGray
import com.stoicus9.snakeclassic.ui.theme.CharcoalBlack
import com.stoicus9.snakeclassic.ui.theme.PanelGray
import com.stoicus9.snakeclassic.ui.theme.SoftWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

@Composable
fun SnakeGameScreen(
    initialHighScore: Int,
    onHighScoreChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = remember { SnakeGameEngine(initialHighScore = initialHighScore) }
    val gameState by engine.state.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestHighScoreSaver by rememberUpdatedState(onHighScoreChanged)

    LaunchedEffect(gameState.status, gameState.tickDelayMillis) {
        if (gameState.status != GameStatus.RUNNING) return@LaunchedEffect

        while (isActive && engine.state.value.status == GameStatus.RUNNING) {
            delay(engine.state.value.tickDelayMillis.toLong())
            val tickResult = engine.tick()

            if (tickResult.ateFood) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            if (tickResult.highScoreChanged) {
                latestHighScoreSaver(engine.state.value.highScore)
            }
            if (tickResult.gameOver) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    DisposableEffect(lifecycleOwner, engine) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                engine.pause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = CharcoalBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Snake Classic",
                color = ArcadeGreen,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            ScoreRow(gameState = gameState)

            Spacer(modifier = Modifier.height(16.dp))

            SnakeBoard(
                gameState = gameState,
                onDirectionChange = engine::changeDirection,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(gameState.columns / gameState.rows.toFloat())
            )

            Spacer(modifier = Modifier.height(18.dp))

            DirectionPad(
                enabled = gameState.status == GameStatus.RUNNING,
                onDirectionChange = engine::changeDirection
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButtons(
                gameState = gameState,
                onStart = engine::start,
                onPause = engine::pause,
                onResume = engine::resume,
                onRestart = engine::restart
            )
        }
    }
}

@Composable
private fun ScoreRow(gameState: GameState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScoreCard(
            title = "Score",
            value = gameState.score.toString(),
            modifier = Modifier.weight(1f)
        )
        ScoreCard(
            title = "High Score",
            value = gameState.highScore.toString(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ScoreCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PanelGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = SoftWhite.copy(alpha = 0.72f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = ArcadeGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SnakeBoard(
    gameState: GameState,
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    val boardShape = RoundedCornerShape(24.dp)
    val controlsEnabled = gameState.status == GameStatus.RUNNING

    Box(
        modifier = modifier
            .clip(boardShape)
            .background(BoardGray, boardShape)
            .border(2.dp, ArcadeGreen.copy(alpha = 0.65f), boardShape)
            .snakeSwipeControls(controlsEnabled, onDirectionChange),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellWidth = size.width / gameState.columns
            val cellHeight = size.height / gameState.rows
            val cellPadding = minOf(cellWidth, cellHeight) * 0.12f

            drawRect(color = BoardGray)

            drawSubtleGrid(
                columns = gameState.columns,
                rows = gameState.rows,
                cellWidth = cellWidth,
                cellHeight = cellHeight
            )

            gameState.snake.asReversed().forEachIndexed { reversedIndex, position ->
                val originalIndex = gameState.snake.lastIndex - reversedIndex
                drawSnakeCell(
                    position = position,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    padding = cellPadding,
                    isHead = originalIndex == 0
                )
            }

            drawFood(
                food = gameState.food,
                cellWidth = cellWidth,
                cellHeight = cellHeight
            )

            drawRoundRect(
                color = ArcadeGreen.copy(alpha = 0.85f),
                style = Stroke(width = 3.dp.toPx()),
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )
        }

        BoardOverlay(gameState = gameState)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSubtleGrid(
    columns: Int,
    rows: Int,
    cellWidth: Float,
    cellHeight: Float
) {
    val gridColor = Color.White.copy(alpha = 0.025f)
    for (col in 1 until columns) {
        val x = col * cellWidth
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
    }
    for (row in 1 until rows) {
        val y = row * cellHeight
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnakeCell(
    position: Position,
    cellWidth: Float,
    cellHeight: Float,
    padding: Float,
    isHead: Boolean
) {
    val color = if (isHead) ArcadeGreen else ArcadeGreenDark
    val left = position.col * cellWidth + padding
    val top = position.row * cellHeight + padding
    val width = cellWidth - padding * 2
    val height = cellHeight - padding * 2
    val radius = minOf(cellWidth, cellHeight) * if (isHead) 0.32f else 0.24f

    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(radius, radius)
    )

    if (isHead) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(left + width * 0.18f, top + height * 0.18f),
            size = Size(width * 0.22f, height * 0.22f),
            cornerRadius = CornerRadius(radius / 2f, radius / 2f)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFood(
    food: Position,
    cellWidth: Float,
    cellHeight: Float
) {
    val radius = minOf(cellWidth, cellHeight) * 0.34f
    val center = Offset(
        x = food.col * cellWidth + cellWidth / 2f,
        y = food.row * cellHeight + cellHeight / 2f
    )

    drawCircle(
        color = ArcadeOrange.copy(alpha = 0.32f),
        radius = radius * 1.5f,
        center = center
    )
    drawCircle(
        color = ArcadeOrange,
        radius = radius,
        center = center
    )
}

@Composable
private fun BoardOverlay(gameState: GameState) {
    val overlayText = when (gameState.status) {
        GameStatus.READY -> "Tap Start to Play"
        GameStatus.PAUSED -> "Paused"
        GameStatus.GAME_OVER -> "Game Over\nFinal Score: ${gameState.score}\nTap Restart to Play Again"
        GameStatus.RUNNING -> null
    }

    if (overlayText != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = overlayText,
                color = SoftWhite,
                fontSize = if (gameState.status == GameStatus.GAME_OVER) 22.sp else 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 31.sp,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

private fun Modifier.snakeSwipeControls(
    enabled: Boolean,
    onDirectionChange: (Direction) -> Unit
): Modifier {
    if (!enabled) return this

    return pointerInput(enabled) {
        val threshold = 24.dp.toPx()
        var drag = Offset.Zero

        detectDragGestures(
            onDragStart = { drag = Offset.Zero },
            onDragEnd = { drag = Offset.Zero },
            onDragCancel = { drag = Offset.Zero },
            onDrag = { change, dragAmount ->
                drag += dragAmount

                if (abs(drag.x) > threshold || abs(drag.y) > threshold) {
                    val direction = if (abs(drag.x) > abs(drag.y)) {
                        if (drag.x > 0) Direction.RIGHT else Direction.LEFT
                    } else {
                        if (drag.y > 0) Direction.DOWN else Direction.UP
                    }

                    onDirectionChange(direction)
                    drag = Offset.Zero
                    change.consume()
                }
            }
        )
    }
}

@Composable
private fun DirectionPad(
    enabled: Boolean,
    onDirectionChange: (Direction) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DirectionButton(
            label = "↑",
            enabled = enabled,
            onClick = { onDirectionChange(Direction.UP) }
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectionButton(
                label = "←",
                enabled = enabled,
                onClick = { onDirectionChange(Direction.LEFT) }
            )
            DirectionButton(
                label = "↓",
                enabled = enabled,
                onClick = { onDirectionChange(Direction.DOWN) }
            )
            DirectionButton(
                label = "→",
                enabled = enabled,
                onClick = { onDirectionChange(Direction.RIGHT) }
            )
        }
    }
}

@Composable
private fun DirectionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(66.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PanelGray,
            contentColor = ArcadeGreen,
            disabledContainerColor = PanelGray.copy(alpha = 0.45f),
            disabledContentColor = SoftWhite.copy(alpha = 0.28f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Text(
            text = label,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActionButtons(
    gameState: GameState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onStart,
                enabled = gameState.status == GameStatus.READY || gameState.status == GameStatus.GAME_OVER,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArcadeGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = PanelGray.copy(alpha = 0.5f),
                    disabledContentColor = SoftWhite.copy(alpha = 0.32f)
                )
            ) {
                Text(text = "Start Game", fontWeight = FontWeight.Bold)
            }

            if (gameState.status == GameStatus.PAUSED) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcadeGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "Resume", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onPause,
                    enabled = gameState.status == GameStatus.RUNNING,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PanelGray,
                        contentColor = ArcadeGreen,
                        disabledContainerColor = PanelGray.copy(alpha = 0.5f),
                        disabledContentColor = SoftWhite.copy(alpha = 0.32f)
                    )
                ) {
                    Text(text = "Pause", fontWeight = FontWeight.Bold)
                }
            }
        }

        OutlinedButton(
            onClick = onRestart,
            enabled = gameState.status != GameStatus.READY || gameState.score > 0,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ArcadeGreen,
                disabledContentColor = SoftWhite.copy(alpha = 0.30f)
            )
        ) {
            Text(text = "Restart", fontWeight = FontWeight.Bold)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = "Swipe on the board or use the buttons.",
        color = SoftWhite.copy(alpha = 0.55f),
        fontSize = 13.sp
    )
}
