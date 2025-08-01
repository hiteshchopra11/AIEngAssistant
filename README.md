# AI English Assistant

A Kotlin Multiplatform application that provides real-time English writing assistance powered by local AI. Built with Compose Multiplatform for Android and iOS.

## Features

- **Real-time Grammar Checking**: Instant feedback on grammatical errors as you type
- **Style Improvements**: Suggestions for better sentence structure and clarity
- **Offline Processing**: All AI processing happens locally (will be powered by C++ via JNI)
- **Multi-platform**: Runs on both Android and iOS
- **Interactive Suggestions**: Apply or ignore suggestions with visual feedback
- **Writing Modes**: Tailored suggestions for Email, Essay, Business, and Casual writing

## Architecture

### Current Implementation
- **Compose Multiplatform UI**: Shared UI code for both platforms
- **Real-time Text Processing**: Immediate highlighting of potential improvements
- **Suggestion Management**: Apply, ignore, or batch-apply suggestions
- **Visual Feedback**: Color-coded suggestions by type (Grammar, Style, Clarity)

### Planned Features
- **Local LLM Integration**: C++ implementation via JNI for offline AI processing
- **Advanced Grammar Rules**: More sophisticated error detection
- **Writing Analytics**: Track improvements over time
- **Custom Dictionaries**: Domain-specific vocabulary support

## Project Structure

- `/composeApp/` - Shared code across platforms using Compose Multiplatform
  - `src/commonMain/` - Platform-agnostic shared code including main UI components
  - `src/androidMain/` - Android-specific implementations
  - `src/iosMain/` - iOS-specific implementations
- `/iosApp/` - iOS application entry point and SwiftUI-specific code

## Getting Started

### Prerequisites
- Android Studio with Kotlin Multiplatform plugin
- Xcode (for iOS development)
- JDK 11 or higher

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

## Key Technologies

- **Kotlin Multiplatform** 2.2.0
- **Compose Multiplatform** 1.8.2
- **Android** - Min SDK 24, Target SDK 35
- **iOS** - Supports x64, arm64, and simulator arm64 architectures
- **Material 3** design system

## Contributing

This project follows Kotlin coding conventions and uses the expect/actual pattern for platform-specific implementations.

## License

This project is open source and available under the [MIT License](LICENSE).

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…