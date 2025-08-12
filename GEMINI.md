# Gemini Project Configuration: AIEnglishAssistant

This file helps Gemini understand the AIEnglishAssistant project better.

## Project Overview

This is a Kotlin Multiplatform project named "AIEnglishAssistant" designed to run on Android, iOS, and Desktop (JVM). It utilizes Compose Multiplatform to share UI and business logic across all target platforms, providing a consistent user experience.

The core shared code is located in `/composeApp/src/commonMain`. Platform-specific implementations are in their respective source sets like `androidMain`, `iosMain`, and `jvmMain`.

## Tech Stack

- **Core Language:** Kotlin
- **Framework:** Kotlin Multiplatform
- **UI:** Compose Multiplatform (Jetpack Compose)
- **Build Tool:** Gradle
- **Key Libraries:**
    - `androidx.lifecycle:lifecycle-viewmodel-compose` (for MVVM pattern)
    - `androidx.activity:activity-compose` (Android integration)
    - `kotlinx-datetime` (for handling dates and times)
    - `kotlinx-coroutines-swing` (for Desktop JVM app)

## Important Commands

- **Build Project:** `./gradlew build`
- **Run Android App:** The recommended way is to select the `composeApp` run configuration and hit 'Run' in Android Studio. Alternatively, you can build the debug APK with `./gradlew :composeApp:assembleDebug`.
- **Run iOS App:** Open the Xcode project located at `iosApp/iosApp.xcodeproj` and run the app from Xcode.
- **Run Desktop App:** `./gradlew :composeApp:run`
- **Run Tests:** `./gradlew check`