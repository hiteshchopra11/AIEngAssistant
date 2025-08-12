package com.ai.english.assistant.mvi

import com.ai.english.assistant.domain.GrammarSuggestionData
import com.ai.english.assistant.domain.WritingSuggestion

// Simple random ID generator for multiplatform compatibility
private fun generateRandomId(): String {
    return (0..6).map { kotlin.random.Random.nextInt(0, 16).toString(16) }.joinToString("")
}


/**
 * Pure function that takes current state and intent, returns new state.
 * This is the core of the MVI architecture - all state transformations happen here.
 */
object WritingAssistantReducer {
    
    fun reduce(currentState: WritingAssistantState, intent: WritingAssistantIntent): WritingAssistantState {
        return when (intent) {
            is WritingAssistantIntent.UpdateText -> {
                val wordCount = if (intent.text.isBlank()) 0 else intent.text.trim().split("\\s+".toRegex()).size
                currentState.copy(
                    text = intent.text,
                    wordCount = wordCount,
                    // Clear suggestions if text is empty
                    suggestions = if (intent.text.isBlank()) emptyList() else currentState.suggestions
                )
            }
            
            is WritingAssistantIntent.SelectWritingMode -> {
                currentState.copy(selectedMode = intent.mode)
            }
            
            is WritingAssistantIntent.ApplySuggestion -> {
                applySuggestion(currentState, intent.suggestion)
            }
            
            is WritingAssistantIntent.ApplyGrammarSuggestion -> {
                applyGrammarSuggestion(currentState, intent.suggestion)
            }
            
            is WritingAssistantIntent.IgnoreSuggestion -> {
                currentState.copy(
                    suggestions = currentState.suggestions.filter { it != intent.suggestion }
                )
            }
            
            is WritingAssistantIntent.RejectGrammarSuggestion -> {
                currentState.copy(
                    grammarSuggestions = currentState.grammarSuggestions.filter { it != intent.suggestion }
                )
            }
            
            is WritingAssistantIntent.RevertEdit -> {
                revertEdit(currentState, intent.editId)
            }
            
            is WritingAssistantIntent.AnalyzeText -> {
                currentState.copy(isAnalyzing = true, error = null)
            }
            
            is WritingAssistantIntent.ClearError -> {
                currentState.copy(error = null)
            }
        }
    }
    
    private fun applySuggestion(state: WritingAssistantState, suggestion: WritingSuggestion): WritingAssistantState {
        val currentText = state.text
        
        // Find the position of the suggestion in the current text
        val (startIndex, endIndex) = findSuggestionPosition(currentText, suggestion)
            ?: return state // Return unchanged if suggestion not found
        
        // Apply the suggestion
        val newText = currentText.replaceRange(startIndex, endIndex, suggestion.suggestion)
        val newWordCount = if (newText.isBlank()) 0 else newText.trim().split("\\s+".toRegex()).size
        
        // Create applied edit record
        val appliedEdit = WritingAssistantState.AppliedEdit(
            id = generateRandomId(),
            originalText = suggestion.original,
            appliedText = suggestion.suggestion,
            startIndex = startIndex,
            endIndex = startIndex + suggestion.suggestion.length
        )
        
        // Update remaining suggestions to account for text changes
        val updatedSuggestions = updateSuggestionsAfterTextChange(
            state.suggestions.filter { it != suggestion },
            startIndex,
            suggestion.original.length,
            suggestion.suggestion.length
        )
        
        return state.copy(
            text = newText,
            wordCount = newWordCount,
            suggestions = updatedSuggestions,
            appliedEdits = state.appliedEdits + appliedEdit
        )
    }
    
    private fun applyGrammarSuggestion(state: WritingAssistantState, suggestion: GrammarSuggestionData): WritingAssistantState {
        val currentText = state.text

        // Find replacement window [start, endExclusive)
        val (replaceStart, replaceEndExclusive) = run {
            val exactIndex = currentText.indexOf(suggestion.original)
            if (exactIndex != -1) {
                exactIndex to (exactIndex + suggestion.original.length)
            } else {
                val approx = findWhitespaceFlexibleMatch(currentText, suggestion.original)
                if (approx != null) approx.first to (approx.last + 1) else null
            }
        } ?: return state

        val newText = currentText.replaceRange(
            replaceStart,
            replaceEndExclusive,
            suggestion.suggestion
        )
        val newWordCount = if (newText.isBlank()) 0 else newText.trim().split("\\s+".toRegex()).size
        
        val appliedEdit = WritingAssistantState.AppliedEdit(
            id = generateRandomId(),
            originalText = suggestion.original,
            appliedText = suggestion.suggestion,
            startIndex = replaceStart,
            endIndex = replaceStart + suggestion.suggestion.length
        )
        
        // Remove alternative suggestions for the same original and any suggestions that no longer exist in the text
        val updatedGrammarSuggestions = state.grammarSuggestions
            .filter { it != suggestion }
            .filter { remaining ->
                // Drop other alternatives that were targeting the same original span
                if (remaining.original == suggestion.original) return@filter false
                // Keep only suggestions whose original still appears (allow flexible whitespace matching)
                newText.contains(remaining.original) || findWhitespaceFlexibleMatch(newText, remaining.original) != null
            }
        
        return state.copy(
            text = newText,
            wordCount = newWordCount,
            grammarSuggestions = updatedGrammarSuggestions,
            appliedEdits = state.appliedEdits + appliedEdit
        )
    }
    
    private fun revertEdit(state: WritingAssistantState, editId: String): WritingAssistantState {
        val edit = state.appliedEdits.find { it.id == editId } ?: return state
        
        // Find the applied text in the current text and revert it
        val currentText = state.text
        val appliedTextIndex = currentText.indexOf(edit.appliedText, edit.startIndex)
        
        if (appliedTextIndex == -1) return state
        
        val revertedText = currentText.replaceRange(
            appliedTextIndex,
            appliedTextIndex + edit.appliedText.length,
            edit.originalText
        )
        val newWordCount = if (revertedText.isBlank()) 0 else revertedText.trim().split("\\s+".toRegex()).size
        
        return state.copy(
            text = revertedText,
            wordCount = newWordCount,
            appliedEdits = state.appliedEdits.filter { it.id != editId }
        )
    }
    
    private fun findSuggestionPosition(text: String, suggestion: WritingSuggestion): Pair<Int, Int>? {
        // Try exact position first
        if (suggestion.startIndex >= 0 && 
            suggestion.endIndex <= text.length &&
            text.substring(suggestion.startIndex, suggestion.endIndex) == suggestion.original) {
            return suggestion.startIndex to suggestion.endIndex
        }
        
        // Fall back to text search
        val foundIndex = text.indexOf(suggestion.original)
        return if (foundIndex >= 0) {
            foundIndex to (foundIndex + suggestion.original.length)
        } else null
    }
    
    /**
     * Updates suggestion indices after a text change to maintain accuracy when applying multiple suggestions
     */
    private fun updateSuggestionsAfterTextChange(
        suggestions: List<WritingSuggestion>,
        changeStartIndex: Int,
        originalLength: Int,
        newLength: Int
    ): List<WritingSuggestion> {
        val offset = newLength - originalLength
        
        return suggestions.map { suggestion ->
            when {
                // Suggestion is completely before the change - no adjustment needed
                suggestion.endIndex <= changeStartIndex -> suggestion
                
                // Suggestion starts after the change - adjust both indices
                suggestion.startIndex >= changeStartIndex + originalLength -> {
                    suggestion.copy(
                        startIndex = suggestion.startIndex + offset,
                        endIndex = suggestion.endIndex + offset
                    )
                }
                
                // Suggestion overlaps with the change - invalidate by setting negative indices
                else -> suggestion.copy(startIndex = -1, endIndex = -1)
            }
        }
    }

    /**
     * Attempts to find a match for [target] inside [text] while treating any space in [target]
     * as a flexible whitespace sequence (one or more spaces/tabs/newlines). Returns the inclusive
     * start..end range if found, or null if not found.
     */
    private fun findWhitespaceFlexibleMatch(text: String, target: String): IntRange? {
        if (target.isBlank()) return null
        val escaped = Regex.escape(target.trim())
        // Replace literal spaces in the escaped pattern with a flexible whitespace matcher
        val pattern = escaped.replace(" ", "\\s+")
        val regex = Regex(pattern)
        val match = regex.find(text) ?: Regex(pattern, RegexOption.IGNORE_CASE).find(text)
        return match?.range
    }
}