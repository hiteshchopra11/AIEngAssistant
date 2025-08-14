package com.ai.english.assistant.mvi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ai.english.assistant.SuggestionContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for Writing Assistant
 * This is the "V" in MVI architecture - manages state and handles intents.
 */
class WritingAssistantViewModel(
    private val suggestionService: SuggestionContract? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    var state by mutableStateOf(WritingAssistantState())
        private set
    
    init {
        // Observe suggestion service state changes
        suggestionService?.grammarSuggestions?.onEach { suggestions ->
            state = state.copy(grammarSuggestions = suggestions)
        }?.launchIn(scope)
        
        suggestionService?.isLoading?.onEach { isLoading ->
            state = state.copy(isAnalyzing = isLoading)
        }?.launchIn(scope)
    }
    
    /**
     * Main entry point for handling user intents
     */
    fun handleIntent(intent: WritingAssistantIntent) {
        val newState = WritingAssistantReducer.reduce(state, intent)
        state = newState
        
        // Handle side effects
        when (intent) {
            is WritingAssistantIntent.AnalyzeText -> {
                analyzeText()
            }
            is WritingAssistantIntent.ApplyGrammarSuggestion -> {
                suggestionService?.acceptSuggestion(intent.suggestion)
            }
            is WritingAssistantIntent.ApplyAllSuggestions -> {
                // Best-effort accept all current suggestions in the service too
                state.grammarSuggestions.forEach { suggestion ->
                    suggestionService?.acceptSuggestion(suggestion)
                }
            }
            is WritingAssistantIntent.RejectGrammarSuggestion -> {
                suggestionService?.rejectSuggestion(intent.suggestion)
            }
            else -> { /* No side effects needed */ }
        }
    }
    
    private fun analyzeText() {
        if (state.text.trim().isEmpty()) return
        
        scope.launch {
            try {
                suggestionService?.clearSuggestions()
                suggestionService?.analyzeText(state.text)
            } catch (e: Exception) {
                state = state.copy(
                    isAnalyzing = false,
                    error = "Failed to analyze text: ${e.message}"
                )
            }
        }
    }
    
    fun cleanup() {
        suggestionService?.cleanup()
    }
}