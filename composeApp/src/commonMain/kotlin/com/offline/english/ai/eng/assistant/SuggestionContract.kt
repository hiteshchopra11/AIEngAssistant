package com.offline.english.ai.eng.assistant

import kotlinx.coroutines.flow.StateFlow

data class WordSuggestionData(val word: String, val confidence: Float = 1.0f)
data class SentenceSuggestionData(val sentence: String, val confidence: Float = 1.0f)

interface SuggestionContract {
    val wordSuggestions: StateFlow<List<WordSuggestionData>>
    val sentenceSuggestions: StateFlow<List<SentenceSuggestionData>>
    val isLoading: StateFlow<Boolean>
    
    fun getWordSuggestions(context: String, currentWord: String = "")
    fun getSentenceSuggestions(context: String)
    fun clearSuggestions()
    fun cleanup()
}