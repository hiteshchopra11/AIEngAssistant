### AI English Assistant

An AI-powered writing helper that improves English grammar, spelling, word choice, and clarity. Built with Kotlin Multiplatform and Compose Multiplatform, it provides a shared UI across Android, iOS, and Desktop. AI suggestions are powered by Firebase AI (Gemini) on Android.


### What it does
- **Grammar and spelling**: Finds issues and proposes concise fixes
- **Word choice and clarity**: Suggests more natural phrasing
- **Inline highlighting**: Highlights problematic text directly in the editor
- **Actionable suggestions**: Apply, skip, or revert each change; apply-all for batches
- **Writing modes**: Switch tone/context (Email, Essay, Business, Casual)
- **Word count**: Live counter as you type


### Architecture

This app follows **MVI (Model-View-Intent)** architecture pattern for predictable state management and unidirectional data flow.

#### MVI Architecture Overview

- **Model (State)**: Single source of truth for UI state (`WritingAssistantState`)
- **View**: Composable UI that observes state and emits intents (`WritingAssistantScreen`, `TextEditor`)
- **Intent**: User actions/intents that trigger state changes (`WritingAssistantIntent`)

#### Core MVI Components

**1. State Management**
```kotlin
// Single immutable state
data class WritingAssistantState(
    val text: String = "",
    val wordCount: Int = 0,
    val selectedMode: WritingMode = WritingMode.EMAIL,
    val suggestions: List<WritingSuggestion> = emptyList(),
    val grammarSuggestions: List<GrammarSuggestionData> = emptyList(),
    val appliedEdits: List<AppliedEdit> = emptyList(),
    val isAnalyzing: Boolean = false,
    val error: String? = null
)
```

**2. User Intents**
```kotlin
// All user actions as sealed interface
sealed interface WritingAssistantIntent {
    data class UpdateText(val text: String) : WritingAssistantIntent
    data class SelectWritingMode(val mode: WritingMode) : WritingAssistantIntent
    data class ApplySuggestion(val suggestion: WritingSuggestion) : WritingAssistantIntent
    object AnalyzeText : WritingAssistantIntent
    // ... more intents
}
```

**3. Pure Reducer Function**
```kotlin
// Pure function: (State, Intent) -> State
object WritingAssistantReducer {
    fun reduce(currentState: WritingAssistantState, intent: WritingAssistantIntent): WritingAssistantState
}
```

**4. ViewModel (Intent Handler)**
```kotlin
// Handles intents and manages side effects
class WritingAssistantViewModel {
    var state by mutableStateOf(WritingAssistantState())
    
    fun handleIntent(intent: WritingAssistantIntent) {
        val newState = WritingAssistantReducer.reduce(state, intent)
        state = newState
        // Handle side effects (API calls, etc.)
    }
}
```

#### How it works (Data Flow)
1. **User Interaction**: User types, clicks button → Intent emitted
2. **Intent Processing**: ViewModel receives intent
3. **State Reduction**: Pure reducer function creates new state
4. **State Update**: ViewModel updates observable state
5. **UI Recomposition**: View observes state and recomposes
6. **Side Effects**: ViewModel handles async operations (API calls)

#### Key Architecture Files

**MVI Layer**:
- `mvi/WritingAssistantIntent.kt` - User intents
- `mvi/WritingAssistantState.kt` - Application state
- `mvi/WritingAssistantReducer.kt` - Pure state transformations
- `mvi/WritingAssistantViewModel.kt` - Intent handler & side effects

**UI Layer**:
- `WritingAssistantScreen.kt` - Main screen composable
- `TextEditorScreen.kt` - Text editor with highlighting

**Domain Layer**:
- `domain/Models.kt` - Domain models and data classes
- `SuggestionContract.kt` - Service interface
- `TextProcessor.kt` - Text analysis utilities

**Platform Layer**:
- `androidMain/.../SuggestionsService.kt` - Firebase AI implementation


### Platform support
- **Android**: Full functionality including AI suggestions (Firebase AI Gemini)
- **iOS and Desktop (JVM)**: UI runs and allows typing; AI suggestions are currently Android-only via `expect`/`actual` `SuggestionContract`


### Run it
- **Build all**: `./gradlew build`
- **Android**: Run from Android Studio or `./gradlew :composeApp:assembleDebug`
- **iOS**: Open `iosApp/iosApp.xcodeproj` in Xcode and run (UI only)
- **Desktop (JVM)**: `./gradlew :composeApp:run` (UI only)


### Android setup (Firebase AI)
To enable AI suggestions on Android, you need to add your Firebase configuration files:

1. **Create Firebase Project**: Go to [Firebase Console](https://console.firebase.google.com/) and create a new project
2. **Enable Firebase AI**: In your Firebase project, enable the Firebase AI service
3. **Download Configuration**: Download the `google-services.json` file for your Android app
4. **Add Configuration Files**:
   - Copy `google-services.json` to `composeApp/src/debug/google-services.json`
   - Copy `google-services.json` to `composeApp/src/release/google-services.json`
   - Template files are provided in the same directories for reference

**⚠️ Security Note**: Firebase configuration files contain API keys and should never be committed to version control. They are already added to `.gitignore`.

This project already includes Firebase AI dependencies via the Gradle Version Catalog. See `FIREBASE.md` for details. The Android service uses the Google AI backend model `gemini-2.5-flash` to produce up to three concise, categorized micro-suggestions for the recently completed sentence or clause.

Additional docs:
- `FIREBASE.md`: Firebase configuration and dependencies
- `TEXT_PROCESSING_FEATURES.md`: Word/sentence detection and AI prompts overview
- `GEMINI.md`: Project overview for AI tooling


### Tech stack
- **Kotlin Multiplatform** with **Compose Multiplatform** UI
- **MVI Architecture** for predictable state management
- **Firebase AI (Gemini)** on Android for AI suggestions
- Gradle Version Catalog for dependency management
- Kotlinx Coroutines for asynchronous operations


### Roadmap ideas
- iOS/Desktop parity for AI suggestions
- More tones and domain presets
- Per-language localization and EFL learning aids
