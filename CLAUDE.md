# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AIEnglishAssistant is a Kotlin Multiplatform project targeting Android, iOS, and Desktop (JVM) platforms using Compose Multiplatform for shared UI. The project integrates Firebase AI for core functionality.

## Architecture

- **Shared Code**: `/composeApp/src/commonMain` - Contains shared business logic and UI components
- **Platform-Specific Code**: 
  - `androidMain` - Android-specific implementations and Firebase integration
  - `iosMain` - iOS-specific implementations  
  - `jvmMain` - Desktop/JVM implementations
- **iOS App**: `/iosApp` - Native iOS app entry point and SwiftUI integration
- **Package**: `com.ai.english.assistant`

## Build Commands

- **Build Project**: `./gradlew build`
- **Run Tests**: `./gradlew check` or `./gradlew test`
- **Clean Build**: `./gradlew clean build`

## Platform-Specific Development

### Android
- **Debug Build**: `./gradlew :composeApp:assembleDebug`
- **Release Build**: `./gradlew :composeApp:assembleRelease`
- Main class: `com.ai.english.assistant.MainActivity`

### iOS  
- Open `iosApp/iosApp.xcodeproj` in Xcode
- Framework name: `ComposeApp` (static framework)
- Supported architectures: x64, arm64, simulator arm64

### Desktop
- **Run Desktop App**: `./gradlew :composeApp:run`
- Main class: `com.ai.english.assistant.MainKt`
- Distribution formats: DMG, MSI, DEB

## Firebase Integration

Firebase is configured via Google Services plugin and uses different config files per build type:
- Debug: `composeApp/src/debug/google-services.json`
- Release: `composeApp/src/release/google-services.json`

Firebase dependencies are Android-specific and use the BOM for version alignment:
```kotlin
// Already configured in androidMain
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.ai)
```

## Dependencies Management

The project uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for centralized dependency management:
- All plugin versions are aliased (e.g., `libs.plugins.kotlinMultiplatform`)
- Library versions are centralized under `[versions]`
- Key dependencies: Compose Multiplatform, Kotlin Coroutines, kotlinx-datetime, Firebase

## Code Organization

- Multiplatform abstractions use `expect`/`actual` pattern (see `Platform.kt`)
- Shared UI components in commonMain use Compose Multiplatform
- Platform-specific implementations handle native integrations
- Firebase AI integration is Android-only via androidMain source set