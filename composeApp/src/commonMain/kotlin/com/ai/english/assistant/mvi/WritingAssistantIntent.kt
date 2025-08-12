package com.ai.english.assistant.mvi

import com.ai.english.assistant.domain.WritingMode
import com.ai.english.assistant.domain.WritingSuggestion
import com.ai.english.assistant.domain.GrammarSuggestionData

/**
 * Represents user intents in the Writing Assistant feature.
 * This is the "I" in MVI architecture - all user interactions are expressed as intents.
 */
sealed interface WritingAssistantIntent {
    data class UpdateText(val text: String) : WritingAssistantIntent
    data class SelectWritingMode(val mode: WritingMode) : WritingAssistantIntent
    data class ApplyGrammarSuggestion(val suggestion: GrammarSuggestionData) : WritingAssistantIntent
    data class RejectGrammarSuggestion(val suggestion: GrammarSuggestionData) : WritingAssistantIntent
    data class RevertEdit(val editId: String) : WritingAssistantIntent
    object AnalyzeText : WritingAssistantIntent
    object ClearError : WritingAssistantIntent
}