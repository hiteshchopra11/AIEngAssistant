package com.ai.english.assistant.mvi

import com.ai.english.assistant.domain.WritingMode
import com.ai.english.assistant.domain.GrammarSuggestionData

/**
 * Represents the complete state of the Writing Assistant feature.
 * This is the "M" in MVI architecture - the single source of truth for UI state.
 */
data class WritingAssistantState(
    val text: String = "",
    val wordCount: Int = 0,
    val selectedMode: WritingMode = WritingMode.EMAIL,
    val grammarSuggestions: List<GrammarSuggestionData> = emptyList(),
    val appliedEdits: List<AppliedEdit> = emptyList(),
    val isAnalyzing: Boolean = false,
    val error: String? = null
) {
    /**
     * Represents an edit that has been applied to the text.
     * Allows for precise tracking and reverting of changes.
     */
    data class AppliedEdit(
        val id: String,
        val originalText: String,
        val appliedText: String,
        val startIndex: Int,
        val endIndex: Int,
        val timestamp: Long = 0L // System.currentTimeMillis() not available in commonMain
    )
}