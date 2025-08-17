# AI English Assistant

An intelligent writing companion that helps improve English grammar, spelling, word choice, and clarity using AI-powered suggestions. Built with Kotlin Multiplatform and Compose Multiplatform for a native experience across Android, iOS, and Desktop platforms.

## 🚀 Features & How It Works

### Real-Time Writing Analysis
**What it does**: As you type, the app analyzes your text and provides intelligent suggestions for improvement.

**Example in action**:
- **Input**: "I have been working on this project since last month and I think it will be ready soon."
- **Grammar suggestion**: `since last month` → `for the past month` (Preposition usage)
- **Word choice**: `I think` → `I believe` (More confident tone)
- **Clarity**: Break long sentence into two for better readability

### Analysis Modes

#### 🚀 Standard Mode (Fast)
- **Model**: Gemini 2.0 Flash Lite
- **Method**: Single-pass streaming analysis
- **Speed**: Real-time results as AI generates suggestions
- **Focus**: Core grammar, spelling, and basic style issues
- **Best for**: Quick proofreading and immediate feedback

#### 🎯 Advanced Mode (Precision)
- **Model**: Gemini 2.0 Flash Experimental  
- **Method**: Multi-iteration analysis with 3 specialized passes
- **Process**:
  1. **Comprehensive Analysis** - Grammar, word choice, punctuation, style, and coherence
  2. **Refinement Pass** - Catches subtle errors missed in first iteration
  3. **Quality Validation** - Filters and validates suggestions for accuracy
- **Accuracy**: Higher precision with up to 200 suggestions
- **Best for**: Important documents requiring thorough review

### 📹 See It In Action

#### **Standard Mode** - Real-time streaming suggestions
<video src="docs/videos/normal-mode-demo.mp4" width="600" controls>
  Your browser does not support the video tag. <a href="docs/videos/normal-mode-demo.mp4">Download the demo video</a>
</video>

*Features shown: Real-time streaming suggestions, fast analysis, basic grammar and style corrections*

#### **Advanced Mode** - Multi-pass comprehensive analysis  
<video src="docs/videos/advanced-mode-demo.mp4" width="600" controls>
  Your browser does not support the video tag. <a href="docs/videos/advanced-mode-demo.mp4">Download the demo video</a>
</video>

*Features shown: Multi-pass analysis, comprehensive suggestions, higher accuracy with detailed explanations*

> **Note**: These videos demonstrate the app running on Android with Firebase AI integration. The analysis speed and suggestion quality differences between modes are clearly visible.

### Interactive Suggestion System
- **Apply**: Accept a suggestion and update your text
- **Skip**: Ignore the suggestion for now
- **Revert**: Undo a previously applied change
- **Apply All**: Accept all current suggestions at once

### Real-Time Text Highlighting
Text is highlighted directly in the editor with color-coded categories:
- 🔴 **Grammar issues**: Red highlighting
- 🟣 **Style improvements**: Purple highlighting  
- 🟢 **Clarity suggestions**: Green highlighting

### Smart Word Counting
Live word counter that formats elegantly:
- `0 words`, `1 word`, `247 words`, `2.1K words`, `1.3M words`

## 🏗️ Architecture

This application follows the **MVI (Model-View-Intent)** architectural pattern, ensuring predictable state management and unidirectional data flow.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
│  ┌─────────────────┐    ┌─────────────────┐                 │
│  │ WritingAssistant│    │   TextEditor    │                 │
│  │     Screen      │    │    Component    │                 │
│  └─────────────────┘    └─────────────────┘                 │
│           │                       │                         │
│           └───────────┬───────────┘                         │
├───────────────────────┼─────────────────────────────────────┤
│                   MVI Layer                                 │
│           ┌───────────▼───────────┐                         │
│           │  WritingAssistant     │                         │
│           │     ViewModel         │                         │
│           └───────────┬───────────┘                         │
│                       │                                     │
│  ┌────────────────────▼────────────────────┐                │
│  │            Intent Processor             │                │
│  │  ┌─────────────┐    ┌─────────────────┐ │                │
│  │  │   Intent    │    │    Reducer      │ │                │
│  │  │  Handler    │    │  (Pure Func)    │ │                │
│  │  └─────────────┘    └─────────────────┘ │                │
│  └─────────────────────┬─────────────────────               │
├────────────────────────┼─────────────────────────────────────┤
│                 Domain Layer                                │
│  ┌─────────────────────▼─────────────────────┐               │
│  │           SuggestionContract              │               │
│  │         (Platform Interface)              │               │
│  └─────────────────────┬─────────────────────┘               │
├────────────────────────┼─────────────────────────────────────┤
│              Platform Layer                                 │
│  ┌─────────────────────▼─────────────────────┐               │
│  │  Android: Firebase AI (Gemini)           │               │
│  │  iOS: Placeholder Implementation          │               │
│  │  JVM: Placeholder Implementation          │               │
│  └───────────────────────────────────────────┘               │
└─────────────────────────────────────────────────────────────┘
```

### MVI Components Breakdown

#### 1. **State (Model)**
Single immutable state holding all UI data:
```kotlin
data class WritingAssistantState(
    val text: String = "",
    val wordCount: Int = 0,
    val grammarSuggestions: List<GrammarSuggestionData> = emptyList(),
    val appliedEdits: List<AppliedEdit> = emptyList(),
    val isAnalyzing: Boolean = false,
    val isAdvancedMode: Boolean = false,
    val error: String? = null
)
```

#### 2. **Intents (User Actions)**
All user interactions as sealed interface:
```kotlin
sealed interface WritingAssistantIntent {
    data class UpdateText(val text: String) : WritingAssistantIntent
    data class ApplyGrammarSuggestion(val suggestion: GrammarSuggestionData) : WritingAssistantIntent
    data class ToggleAdvancedMode(val enabled: Boolean) : WritingAssistantIntent
    object AnalyzeText : WritingAssistantIntent
    object ApplyAllSuggestions : WritingAssistantIntent
    // ... more intents
}
```

#### 3. **Reducer (Pure State Transformation)**
```kotlin
object WritingAssistantReducer {
    fun reduce(
        currentState: WritingAssistantState, 
        intent: WritingAssistantIntent
    ): WritingAssistantState {
        return when (intent) {
            is WritingAssistantIntent.UpdateText -> 
                currentState.copy(
                    text = intent.text,
                    wordCount = intent.text.split("\\s+".toRegex()).size
                )
            // ... handle other intents
        }
    }
}
```

#### 4. **ViewModel (Orchestrator)**
Coordinates between UI, state, and services:
```kotlin
class WritingAssistantViewModel(
    private val suggestionService: SuggestionContract
) {
    var state by mutableStateOf(WritingAssistantState())
        private set
    
    fun handleIntent(intent: WritingAssistantIntent) {
        state = WritingAssistantReducer.reduce(state, intent)
        handleSideEffects(intent)
    }
}
```

### Data Flow
1. **User Interaction** → Intent emitted from UI
2. **Intent Processing** → ViewModel receives intent
3. **State Reduction** → Pure function creates new state
4. **State Update** → ViewModel updates observable state  
5. **UI Recomposition** → Compose UI observes state changes
6. **Side Effects** → Async operations (API calls) handled separately

## 🛠️ Tech Stack

### Core Technologies
- **[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)** - Shared business logic across platforms
- **[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)** - Shared UI framework
- **[Firebase AI (Gemini)](https://firebase.google.com/docs/vertex-ai)** - AI-powered text analysis on Android
- **[Gradle Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)** - Centralized dependency management

### Architecture & Patterns
- **MVI (Model-View-Intent)** - Unidirectional data flow
- **Dependency Injection** - Platform-specific implementations
- **Expect/Actual** - Kotlin Multiplatform abstractions

### Development Tools
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx DateTime** - Date/time operations
- **Android Splash Screen API** - Proper splash screen implementation
- **Material 3 Design** - Modern UI components

## 📁 Project Structure

```
AIEnglishAssistant/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/kotlin/         # Shared code
│   │   │   ├── domain/                # Business models
│   │   │   ├── mvi/                   # MVI architecture
│   │   │   ├── *.kt                   # UI components
│   │   ├── androidMain/kotlin/        # Android-specific
│   │   │   ├── MainActivity.kt        # Android entry point
│   │   │   ├── SuggestionsService.kt  # Firebase AI implementation
│   │   ├── iosMain/kotlin/            # iOS-specific
│   │   ├── jvmMain/kotlin/            # Desktop-specific
│   │   ├── debug/                     # Debug Firebase config
│   │   └── release/                   # Release Firebase config
├── iosApp/                            # iOS Xcode project
├── gradle/libs.versions.toml          # Dependency catalog
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- **JDK 11+** for Kotlin compilation
- **Android Studio** for Android development
- **Xcode** for iOS development (macOS only)

### Build Commands
```bash
# Build entire project
./gradlew build

# Clean build
./gradlew clean build

# Run tests
./gradlew check
```

### Platform-Specific Commands

#### Android
```bash
# Debug build
./gradlew :composeApp:assembleDebug

# Release build  
./gradlew :composeApp:assembleRelease

# Install on device
./gradlew :composeApp:installDebug
```

#### iOS
```bash
# Open in Xcode
open iosApp/iosApp.xcodeproj
```

#### Desktop (JVM)
```bash
# Run desktop application
./gradlew :composeApp:run
```

## 🔐 Firebase AI Setup (Android)

To enable AI suggestions on Android:

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create new project or use existing
3. Enable **Vertex AI in Firebase**

### 2. Configure Android App
1. Add Android app to Firebase project
2. Use package name: `com.ai.english.assistant`
3. Download `google-services.json`

### 3. Add Configuration Files
```bash
# Place the downloaded file in both locations:
cp google-services.json composeApp/src/debug/
cp google-services.json composeApp/src/release/
```

### 4. AI Models Used
The app utilizes different Gemini models based on the selected mode:

- **Standard Mode**: `gemini-2.0-flash-lite` - Optimized for speed and real-time streaming
- **Advanced Mode**: `gemini-2.0-flash-exp` - Experimental model with enhanced accuracy

Both models are accessed through Firebase AI with specialized prompts for grammar, style, and clarity analysis.

### 5. Security Notes
⚠️ **Important**: Firebase configuration files contain API keys and are excluded from version control via `.gitignore`

## 🎯 Platform Support

| Platform | UI Support | AI Suggestions | Status |
|----------|------------|----------------|--------|
| **Android** | ✅ Full | ✅ Firebase AI | Production Ready |
| **iOS** | ✅ Full | ⏳ Planned | UI Complete |
| **Desktop** | ✅ Full | ⏳ Planned | UI Complete |

## 🔮 Roadmap

### Near Term
- [ ] iOS AI integration (On-device Core ML)
- [ ] Desktop AI integration (Local LLM)
- [ ] Enhanced writing modes (Academic, Creative, Technical)
- [ ] Dark mode support

### Future Features
- [ ] Multi-language support
- [ ] Document export (PDF, Word)
- [ ] Writing analytics and insights
- [ ] Collaborative editing
- [ ] Plugin system for custom suggestions

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using Kotlin Multiplatform & Compose Multiplatform**