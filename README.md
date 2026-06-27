# Snake Classic

A clean native Android Snake game built with Kotlin and Jetpack Compose.

## What is included

- Kotlin native Android app
- Jetpack Compose UI
- Custom Compose Canvas board drawing
- Swipe controls on the game board
- On-screen D-pad controls
- Ready, Running, Paused, and Game Over states
- Score and persistent high score using SharedPreferences
- Speed increase every 50 points, capped at a playable minimum speed
- Haptic feedback on food eat and game over
- Automatic pause when the app goes to background
- Portrait-first responsive layout

## How to run

1. Open Android Studio.
2. Choose **File > Open**.
3. Select the `SnakeClassic` folder.
4. Let Android Studio sync Gradle.
5. Select an emulator or real Android device.
6. Press **Run**.

## Requirements

- Android Studio with Android Gradle Plugin support
- Android SDK Platform 35 installed, or update `compileSdk`/`targetSdk` in `app/build.gradle.kts` to match a newer installed stable SDK
- JDK 17

## Main files

- `app/src/main/java/com/stoicus9/snakeclassic/MainActivity.kt`  
  Loads the saved high score and launches the Compose screen.

- `app/src/main/java/com/stoicus9/snakeclassic/ui/SnakeGameScreen.kt`  
  Main Compose UI, board rendering, swipe handling, D-pad controls, action buttons, lifecycle pause, and game loop.

- `app/src/main/java/com/stoicus9/snakeclassic/game/SnakeGameEngine.kt`  
  Core Snake logic: movement, direction validation, collision detection, food spawning, scoring, speed, restart, pause, resume.

- `app/src/main/java/com/stoicus9/snakeclassic/model/`  
  Data models and enums: `Position`, `Direction`, `GameStatus`, and `GameState`.

- `app/src/main/java/com/stoicus9/snakeclassic/ui/theme/`  
  Simple dark arcade theme and colors.

## Assumptions made

- The board uses a 20 x 30 grid.
- The app is portrait-first.
- High score uses SharedPreferences instead of DataStore to keep the project lightweight.
- Audio is skipped to avoid requiring sound assets.
- The project uses stable, broadly compatible Android/Compose versions.

## Known limitations

- No sound effects are included.
- No landscape-specific UI optimization is included.
- Gradle wrapper files are not included, so Android Studio should use its configured Gradle installation or generate a wrapper during sync.

## Direct phone testing without Android Studio

Use the included GitHub Actions workflow:

`.github/workflows/build-apk.yml`

Upload this project to GitHub, run **Build Android APK**, download the `SnakeClassic-debug-apk` artifact, extract `app-debug.apk`, then install it on your phone.

See `APK_DIRECT_TEST_GUIDE.md` for step-by-step instructions.
