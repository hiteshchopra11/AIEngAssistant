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
        // Only observe loading state from the service StateFlow
        suggestionService?.isLoading?.onEach { isLoading ->
            state = state.copy(isAnalyzing = isLoading)
        }?.launchIn(scope)
        
        // Observe streaming suggestions for real-time updates - this is our primary source of suggestions
        suggestionService?.streamingSuggestions?.onEach { newSuggestion ->
            // Add the new suggestion to the current list immediately
            val currentSuggestions = state.grammarSuggestions.toMutableList()
            currentSuggestions.add(newSuggestion)
            state = state.copy(grammarSuggestions = currentSuggestions)
            println("🎯 ViewModel added suggestion: ${newSuggestion.original} → ${newSuggestion.suggestion} (total: ${currentSuggestions.size})")
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
                // Clear existing suggestions in ViewModel state immediately
                state = state.copy(grammarSuggestions = emptyList())
                
                suggestionService?.clearSuggestions()
                suggestionService?.analyzeText(state.text, state.isAdvancedMode)
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