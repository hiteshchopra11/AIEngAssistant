package com.offline.english.ai.eng.assistant.mvi

import com.offline.english.ai.eng.assistant.WritingMode
import com.offline.english.ai.eng.assistant.WritingSuggestion

data class WritingAssistantViewState(
    val text: String = "",
    val wordCount: Int = 0,
    val selectedMode: WritingMode = WritingMode.EMAIL,
    val suggestions: List<WritingSuggestion> = emptyList(),
    val appliedSuggestions: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val activeSuggestions: List<WritingSuggestion>
        get() = suggestions.filterIndexed { index, suggestion ->
            !appliedSuggestions.contains(index) &&
                    suggestion.startIndex < text.length &&
                    suggestion.endIndex <= text.length
        }
}