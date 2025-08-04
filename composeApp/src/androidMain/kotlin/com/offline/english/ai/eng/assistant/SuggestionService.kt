package com.offline.english.ai.eng.assistant

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class SuggestionService(context: Context) : SuggestionContract {
    private val gemmaInference = GemmaInference(context)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "SuggestionService"
    
    private val _wordSuggestions = MutableStateFlow<List<WordSuggestionData>>(emptyList())
    override val wordSuggestions: StateFlow<List<WordSuggestionData>> = _wordSuggestions.asStateFlow()
    
    private val _sentenceSuggestions = MutableStateFlow<List<SentenceSuggestionData>>(emptyList())
    override val sentenceSuggestions: StateFlow<List<SentenceSuggestionData>> = _sentenceSuggestions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var currentWordJob: Job? = null
    private var currentSentenceJob: Job? = null
    
    init {
        coroutineScope.launch {
            val initialized = gemmaInference.initialize()
            if (!initialized) {
                Log.e(TAG, "Failed to initialize Gemma inference")
            }
        }
    }
    
    override fun getWordSuggestions(context: String, currentWord: String) {
        currentWordJob?.cancel()

        currentWordJob = coroutineScope.launch {
            try {
                _isLoading.value = true
                val suggestions = fetchWordSuggestions(context, currentWord)
                _wordSuggestions.value = suggestions
            } catch (e: Exception) {
                Log.e(TAG, "Error getting word suggestions: ${e.message}")
                _wordSuggestions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    override fun getSentenceSuggestions(context: String) {
        currentSentenceJob?.cancel()
        currentSentenceJob = coroutineScope.launch {
            try {
                _isLoading.value = true
                val suggestions = fetchSentenceSuggestions(context)
                _sentenceSuggestions.value = suggestions
            } catch (e: Exception) {
                Log.e(TAG, "Error getting sentence suggestions: ${e.message}")
                _sentenceSuggestions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun fetchWordSuggestions(context: String, currentWord: String): List<WordSuggestionData> {
        return withContext(Dispatchers.IO) {
            withTimeout(5000) { // 5 second timeout for word suggestions
                val prompt = buildWordPrompt(context, currentWord)
                val response = gemmaInference.generate(prompt)
                parseWordSuggestions(response)
            }
        }
    }
    
    private suspend fun fetchSentenceSuggestions(context: String): List<SentenceSuggestionData> {
        return withContext(Dispatchers.IO) {
            withTimeout(10000) { // 10 second timeout for sentence suggestions
                val prompt = buildSentencePrompt(context)
                val response = gemmaInference.generate(prompt)
                parseSentenceSuggestions(response)
            }
        }
    }
    
    private fun buildWordPrompt(context: String, currentWord: String): String {
        return if (currentWord.isNotEmpty()) {
            """Complete this word: "$currentWord" in the context: "$context"
            
            Provide only 1-3 word completions, one per line:"""
        } else {
            """Suggest the next 1-3 words that would naturally follow this text: "$context"
            
            Provide only word suggestions, one per line:"""
        }
    }
    
    private fun buildSentencePrompt(context: String): String {
        return """Complete this sentence or suggest a better way to write it: "$context"
        
        Provide 2-3 sentence suggestions, one per line:"""
    }
    
    private fun parseWordSuggestions(response: String): List<WordSuggestionData> {
        if (response.startsWith("Error:")) {
            Log.w(TAG, "Error in word suggestion response: $response")
            return emptyList()
        }
        
        return response.lines()
            .filter { it.isNotBlank() && !it.startsWith("Error") }
            .take(3)
            .map { line ->
                val cleanWord = line.trim()
                    .removePrefix("-")
                    .removePrefix("*")
                    .removePrefix("1.")
                    .removePrefix("2.")
                    .removePrefix("3.")
                    .trim()
                    .split(" ")
                    .take(3)
                    .joinToString(" ")
                WordSuggestionData(cleanWord)
            }
            .filter { it.word.isNotBlank() }
    }
    
    private fun parseSentenceSuggestions(response: String): List<SentenceSuggestionData> {
        if (response.startsWith("Error:")) {
            Log.w(TAG, "Error in sentence suggestion response: $response")
            return emptyList()
        }
        
        return response.lines()
            .filter { it.isNotBlank() && !it.startsWith("Error") }
            .take(3)
            .map { line ->
                val cleanSentence = line.trim()
                    .removePrefix("-")
                    .removePrefix("*")
                    .removePrefix("1.")
                    .removePrefix("2.")
                    .removePrefix("3.")
                    .trim()
                SentenceSuggestionData(cleanSentence)
            }
            .filter { it.sentence.isNotBlank() }
    }
    
    override fun clearSuggestions() {
        currentWordJob?.cancel()
        currentSentenceJob?.cancel()
        _wordSuggestions.value = emptyList()
        _sentenceSuggestions.value = emptyList()
    }
    
    override fun cleanup() {
        coroutineScope.cancel()
        gemmaInference.cleanup()
    }
}