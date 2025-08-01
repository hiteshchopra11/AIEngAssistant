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
    val isProcessingWord: Boolean = false,
    val isProcessingSentence: Boolean = false,
    val lastProcessedWordIndex: Int = -1,
    val lastProcessedSentenceIndex: Int = -1,
    val error: String? = null
) {
    val activeSuggestions: List<WritingSuggestion>
        get() = suggestions.filterIndexed { index, suggestion ->
            !appliedSuggestions.contains(index) &&
                    suggestion.startIndex < text.length &&
                    suggestion.endIndex <= text.length
        }
}