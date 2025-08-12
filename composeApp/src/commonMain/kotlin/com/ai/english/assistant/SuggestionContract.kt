package com.ai.english.assistant

import kotlinx.coroutines.flow.StateFlow
import com.ai.english.assistant.domain.GrammarSuggestionData


interface SuggestionContract {
    val grammarSuggestions: StateFlow<List<GrammarSuggestionData>>
    val isLoading: StateFlow<Boolean>

    fun analyzeText(fullText: String)
    fun acceptSuggestion(suggestion: GrammarSuggestionData)
    fun rejectSuggestion(suggestion: GrammarSuggestionData)
    fun clearSuggestions()
    fun cleanup()
}