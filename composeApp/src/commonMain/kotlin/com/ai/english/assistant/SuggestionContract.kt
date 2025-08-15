package com.ai.english.assistant

import com.ai.english.assistant.domain.GrammarSuggestionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


interface SuggestionContract {
    val grammarSuggestions: StateFlow<List<GrammarSuggestionData>>
    val isLoading: StateFlow<Boolean>
    val streamingSuggestions: Flow<GrammarSuggestionData>

    fun analyzeText(fullText: String, isAdvancedMode: Boolean = false)
    fun acceptSuggestion(suggestion: GrammarSuggestionData)
    fun rejectSuggestion(suggestion: GrammarSuggestionData)
    fun clearSuggestions()
    fun cleanup()
}