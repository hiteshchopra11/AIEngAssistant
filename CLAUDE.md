# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform project targeting Android and iOS using Compose Multiplatform. The application is an AI English Assistant with the package name `com.offline.english.ai.eng.assistant`.

## Project Structure

- `/composeApp/` - Shared code across platforms using Compose Multiplatform
  - `src/commonMain/` - Platform-agnostic shared code
  - `src/androidMain/` - Android-specific implementations
  - `src/iosMain/` - iOS-specific implementations
  - `src/commonTest/` - Shared unit tests
- `/iosApp/` - iOS application entry point and SwiftUI-specific code
- `/gradle/libs.versions.toml` - Centralized dependency version management

## Key Technologies

- **Kotlin Multiplatform** 2.2.0
- **Compose Multiplatform** 1.8.2
- **Android** - Min SDK 24, Target SDK 35, Compile SDK 35
- **iOS** - Supports x64, arm64, and simulator arm64 architectures
- **Gradle** with version catalogs and configuration cache enabled

## Common Development Commands

### Building the Project
```bash
# Build all targets
./gradlew build

# Build Android only
./gradlew :composeApp:assembleDebug

# Build iOS framework
./gradlew :composeApp:linkDebugFrameworkIosX64
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run common tests only
./gradlew :composeApp:commonTest
```

### Development Setup
```bash
# Clean build
./gradlew clean

# Generate resources
./gradlew :composeApp:generateComposeResClass
```

## Architecture Patterns

### Platform Abstraction
The project uses the `expect`/`actual` pattern for platform-specific implementations:
- `Platform.kt` in `commonMain` defines the expected interface
- Platform-specific implementations in `androidMain` and `iosMain`

### UI Architecture
- Single `App.kt` composable as the main UI entry point
- Material 3 design system
- Compose multiplatform resources for cross-platform assets

### Code Organization
- Package structure: `com.offline.english.ai.eng.assistant`
- Platform-specific code is isolated in respective source sets
- Shared business logic and UI in `commonMain`

## Build Configuration Notes

- Uses Gradle version catalogs (`libs.versions.toml`) for dependency management
- Configuration cache and build cache are enabled for performance
- JVM target is set to Java 11
- iOS framework is configured as a static library with base name "ComposeApp"