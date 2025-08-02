package com.offline.english.ai.eng.assistant.mvi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.offline.english.ai.eng.assistant.WritingSuggestion

/**
 * Clean MVI ViewModel for Writing Assistant
 */
class WritingAssistantViewModel {
    
    var state by mutableStateOf(WritingAssistantViewState())
        private set

    fun handleAction(action: WritingAssistantViewAction) {
        when (action) {
            is WritingAssistantViewAction.UpdateText -> {
                updateText(action.text)
            }
            is WritingAssistantViewAction.SelectWritingMode -> {
                state = state.copy(selectedMode = action.mode)
            }
            is WritingAssistantViewAction.ApplySuggestion -> {
                applySuggestion(action.suggestion)
            }
            is WritingAssistantViewAction.IgnoreSuggestion -> {
                ignoreSuggestion(action.suggestion)
            }
            is WritingAssistantViewAction.ApplyAllSuggestions -> {
                applyAllSuggestions()
            }
            is WritingAssistantViewAction.GenerateSuggestions -> {
                // TODO: Implement with MediaPipe
            }
            is WritingAssistantViewAction.ProcessWordLevel -> {
                // TODO: Implement with MediaPipe
            }
            is WritingAssistantViewAction.ProcessSentenceLevel -> {
                // TODO: Implement with MediaPipe
            }
            is WritingAssistantViewAction.ClearError -> {
                state = state.copy(error = null)
            }
        }
    }

    private fun updateText(text: String) {
        val wordCount = if (text.isBlank()) 0 else text.trim().split("\\s+".toRegex()).size
        
        state = state.copy(
            text = text,
            wordCount = wordCount
        )
        
        if (text.isBlank()) {
            state = state.copy(suggestions = emptyList(), appliedSuggestions = emptySet())
        }
        
        // TODO: Add word/sentence completion detection for MediaPipe processing
    }

    private fun applySuggestion(suggestion: WritingSuggestion) {
        val originalIndex = state.suggestions.indexOf(suggestion)
        if (originalIndex != -1) {
            val newText = state.text.replaceRange(
                suggestion.startIndex,
                suggestion.endIndex,
                suggestion.suggestion
            )
            val newWordCount = if (newText.isBlank()) 0 else newText.trim().split("\\s+".toRegex()).size
            
            state = state.copy(
                text = newText,
                wordCount = newWordCount,
                appliedSuggestions = state.appliedSuggestions + originalIndex
            )
        }
    }

    private fun ignoreSuggestion(suggestion: WritingSuggestion) {
        val originalIndex = state.suggestions.indexOf(suggestion)
        if (originalIndex != -1) {
            state = state.copy(
                appliedSuggestions = state.appliedSuggestions + originalIndex
            )
        }
    }

    private fun applyAllSuggestions() {
        var updatedText = state.text
        var offset = 0
        
        state.activeSuggestions.sortedBy { it.startIndex }.forEach { suggestion ->
            val adjustedStart = suggestion.startIndex + offset
            val adjustedEnd = suggestion.endIndex + offset
            updatedText = updatedText.replaceRange(
                adjustedStart,
                adjustedEnd,
                suggestion.suggestion
            )
            offset += suggestion.suggestion.length - (suggestion.endIndex - suggestion.startIndex)
        }
        
        val newWordCount = if (updatedText.isBlank()) 0 else updatedText.trim().split("\\s+".toRegex()).size
        
        state = state.copy(
            text = updatedText,
            wordCount = newWordCount,
            appliedSuggestions = state.suggestions.indices.toSet()
        )
    }
}