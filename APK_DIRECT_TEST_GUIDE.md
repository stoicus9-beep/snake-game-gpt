# Test Snake Classic directly on Android phone without Android Studio

This project already includes a GitHub Actions workflow that builds a debug APK in the cloud.
You do not need Android Studio to test it on your phone.

## Method 1: Build APK using GitHub Actions

1. Extract `SnakeClassic.zip`.
2. Create a new GitHub repository.
3. Upload/push the extracted project files to that repository.
4. Open the repository on GitHub.
5. Go to the **Actions** tab.
6. Open **Build Android APK**.
7. Click **Run workflow**.
8. After the build finishes, open the completed workflow run.
9. Download the artifact named **SnakeClassic-debug-apk**.
10. Extract it. Inside you will get:

   `app-debug.apk`

11. Send `app-debug.apk` to your Android phone.
12. On your phone, open the APK and install it.

## Allow APK installation on Android

If Android blocks installation:

1. Open **Settings**.
2. Go to **Security** or **Privacy**.
3. Open **Install unknown apps**.
4. Allow your browser/file manager/WhatsApp/Telegram to install APKs.
5. Try opening the APK again.

## Method 2: Build from command line without Android Studio

If you have Java, Android SDK, and Gradle installed, run this from the project folder:

```bash
gradle assembleDebug
```

The APK will be created here:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Important

The generated debug APK is for testing only. For Play Store release, create a signed release APK/AAB.
