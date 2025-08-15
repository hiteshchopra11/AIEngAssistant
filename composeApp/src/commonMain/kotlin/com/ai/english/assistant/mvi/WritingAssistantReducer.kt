package com.ai.english.assistant.mvi

import com.ai.english.assistant.domain.GrammarSuggestionData

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
                    wordCount = wordCount
                )
            }
            
            is WritingAssistantIntent.SelectWritingMode -> {
                currentState.copy(selectedMode = intent.mode)
            }
            
            is WritingAssistantIntent.ApplyGrammarSuggestion -> {
                applyGrammarSuggestion(currentState, intent.suggestion)
            }
            
            is WritingAssistantIntent.ApplyAllSuggestions -> {
                var nextState = currentState
                // Apply each suggestion in reverse order to avoid index shifting issues
                for (suggestion in currentState.grammarSuggestions.reversed()) {
                    nextState = applyGrammarSuggestion(nextState, suggestion)
                }
                nextState
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
            
            is WritingAssistantIntent.ToggleAdvancedMode -> {
                currentState.copy(isAdvancedMode = intent.isAdvancedMode)
            }
        }
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