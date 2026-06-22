# Bufferless Video Player — Android

A didactic Android app built with **Kotlin**, **Jetpack Compose**, and **ExoPlayer** to demonstrate how to stream and play videos with minimal buffering on Android.

## Purpose

This project is intended as a learning resource. It walks through key Android development concepts in a practical, hands-on way:

- **Jetpack Compose** — declarative UI with `@Composable` functions, state hoisting, and `remember`/`mutableStateOf`
- **Navigation Compose** — type-safe navigation between screens using `NavHost` and `composable<Route>`
- **ExoPlayer (Media3)** — streaming video playback with low-latency buffer configuration
- **Kotlin Serialization** — passing complex objects as navigation arguments

## App Flow

```
HomeScreen  →  (user enters a video URL)  →  PlayerScreen
```

1. **HomeScreen** — a URL input field and a Play button. The button is disabled while the field is empty, demonstrating Compose state (`enabled = urlText.isNotBlank()`).
2. **PlayerScreen** — receives the URL via the navigation back stack and plays the video using ExoPlayer.

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (type-safe routes) |
| Video Playback | ExoPlayer (AndroidX Media3) |
| Serialization | kotlinx.serialization |
| Min SDK | API 24 (Android 7.0) |

## Getting Started

1. Clone the repo:
   ```bash
   git clone https://github.com/fabianz66/bufferless-video-player-android.git
   ```
2. Open in **Android Studio Meerkat** or later.
3. Run on an emulator or physical device (API 24+).
4. Paste any public `.mp4` or HLS `.m3u8` URL into the text field and tap **Open Video Player**.

## Project Structure

```
app/src/main/java/engineer/zamora/bufferlessvideoplayer/
├── MainActivity.kt
├── navigation/
│   ├── AppNavigation.kt   # NavHost wiring
│   └── Routes.kt          # Type-safe route definitions
└── ui/
    ├── screens/
    │   ├── HomeScreen.kt   # URL input + navigate
    │   └── PlayerScreen.kt # ExoPlayer playback
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## License

MIT
