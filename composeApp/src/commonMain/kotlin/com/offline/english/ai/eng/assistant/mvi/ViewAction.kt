package com.offline.english.ai.eng.assistant.mvi

import com.offline.english.ai.eng.assistant.WritingMode
import com.offline.english.ai.eng.assistant.WritingSuggestion

sealed class WritingAssistantViewAction {
    data class UpdateText(val text: String) : WritingAssistantViewAction()
    data class SelectWritingMode(val mode: WritingMode) : WritingAssistantViewAction()
    data class ApplySuggestion(val suggestion: WritingSuggestion) : WritingAssistantViewAction()
    data class IgnoreSuggestion(val suggestion: WritingSuggestion) : WritingAssistantViewAction()
    object ApplyAllSuggestions : WritingAssistantViewAction()
    object GenerateSuggestions : WritingAssistantViewAction()
    data class ProcessWordLevel(val word: String, val startIndex: Int) : WritingAssistantViewAction()
    data class ProcessSentenceLevel(val sentence: String, val startIndex: Int) : WritingAssistantViewAction()
    object ClearError : WritingAssistantViewAction()
}