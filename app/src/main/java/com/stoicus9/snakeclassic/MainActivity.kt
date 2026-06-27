package com.stoicus9.snakeclassic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stoicus9.snakeclassic.ui.SnakeGameScreen
import com.stoicus9.snakeclassic.ui.theme.SnakeClassicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val initialHighScore = preferences.getInt(KEY_HIGH_SCORE, 0)

        setContent {
            SnakeClassicTheme {
                SnakeGameScreen(
                    initialHighScore = initialHighScore,
                    onHighScoreChanged = { highScore ->
                        preferences.edit().putInt(KEY_HIGH_SCORE, highScore).apply()
                    }
                )
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "snake_classic_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
    }
}
