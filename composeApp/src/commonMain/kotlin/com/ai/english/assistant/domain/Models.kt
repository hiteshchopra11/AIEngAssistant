package com.ai.english.assistant.domain

/**
 * Domain models for the Writing Assistant feature
 */

enum class WritingMode {
    EMAIL,
    ESSAY,
    CREATIVE,
    BUSINESS,
    ACADEMIC
}

enum class WritingSuggestionType {
    GRAMMAR,
    STYLE,
    CLARITY
}

enum class SuggestionType {
    GRAMMAR_WORD,
    GRAMMAR_SENTENCE,
    SPELLING,
    STYLE,
    CLARITY
}

data class WritingSuggestion(
    val original: String,
    val suggestion: String,
    val startIndex: Int,
    val endIndex: Int,
    val type: WritingSuggestionType
)

data class GrammarSuggestionData(
    val original: String,
    val suggestion: String,
    val type: SuggestionType,
    val confidence: Float = 1.0f,
    val timestamp: Long = 0L,
    val category: String = "",
    val explanation: String = ""
)