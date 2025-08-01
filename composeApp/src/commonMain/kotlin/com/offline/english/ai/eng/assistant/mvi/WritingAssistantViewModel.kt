package com.offline.english.ai.eng.assistant.mvi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.offline.english.ai.eng.assistant.SuggestionType
import com.offline.english.ai.eng.assistant.WritingSuggestion
import com.offline.english.ai.eng.assistant.ai.LLMProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class WritingAssistantViewModel {
    var state by mutableStateOf(WritingAssistantViewState())
        private set
    
    private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val llmInterface = LLMProvider.getInstance()
    private var wordProcessingJob: Job? = null
    private var sentenceProcessingJob: Job? = null

    fun handleAction(action: WritingAssistantViewAction) {
        when (action) {
            is WritingAssistantViewAction.UpdateText -> updateText(action.text)
            is WritingAssistantViewAction.SelectWritingMode -> selectWritingMode(action.mode)
            is WritingAssistantViewAction.ApplySuggestion -> applySuggestion(action.suggestion)
            is WritingAssistantViewAction.IgnoreSuggestion -> ignoreSuggestion(action.suggestion)
            is WritingAssistantViewAction.ApplyAllSuggestions -> applyAllSuggestions()
            is WritingAssistantViewAction.GenerateSuggestions -> generateSuggestions()
            is WritingAssistantViewAction.ProcessWordLevel -> processWordLevel(action.word, action.startIndex)
            is WritingAssistantViewAction.ProcessSentenceLevel -> processSentenceLevel(action.sentence, action.startIndex)
            is WritingAssistantViewAction.ClearError -> clearError()
        }
    }

    private fun updateText(text: String) {
        val previousText = state.text
        val wordCount = if (text.isBlank()) 0 else text.split(" ").filter { it.isNotBlank() }.size
        
        state = state.copy(
            text = text,
            wordCount = wordCount
        )
        
        // Detect if a word was just completed (space was added)
        if (text.length > previousText.length && text.endsWith(" ")) {
            val lastSpaceIndex = text.lastIndexOf(" ", text.length - 2)
            val wordStartIndex = if (lastSpaceIndex == -1) 0 else lastSpaceIndex + 1
            val wordEndIndex = text.length - 1
            val completedWord = text.substring(wordStartIndex, wordEndIndex).trim()
            
            if (completedWord.isNotBlank() && wordStartIndex != state.lastProcessedWordIndex) {
                handleAction(WritingAssistantViewAction.ProcessWordLevel(completedWord, wordStartIndex))
            }
        }
        
        // Detect if a sentence was completed
        if (text.length > previousText.length && (text.endsWith(". ") || text.endsWith("! ") || text.endsWith("? "))) {
            val lastSentenceEnd = maxOf(
                text.lastIndexOf(". ", text.length - 3),
                text.lastIndexOf("! ", text.length - 3),
                text.lastIndexOf("? ", text.length - 3)
            )
            val sentenceStartIndex = if (lastSentenceEnd == -1) 0 else lastSentenceEnd + 2
            val sentence = text.substring(sentenceStartIndex, text.length - 1).trim()
            
            if (sentence.isNotBlank() && sentenceStartIndex != state.lastProcessedSentenceIndex) {
                handleAction(WritingAssistantViewAction.ProcessSentenceLevel(sentence, sentenceStartIndex))
            }
        }
    }

    private fun selectWritingMode(mode: com.offline.english.ai.eng.assistant.WritingMode) {
        state = state.copy(selectedMode = mode)
        generateSuggestions()
    }

    private fun applySuggestion(suggestion: WritingSuggestion) {
        val originalIndex = state.suggestions.indexOf(suggestion)
        if (originalIndex != -1) {
            val newText = state.text.replaceRange(
                suggestion.startIndex,
                suggestion.endIndex,
                suggestion.suggestion
            )
            val newWordCount = if (newText.isBlank()) 0 else newText.split(" ").filter { it.isNotBlank() }.size
            
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
        
        val newWordCount = if (updatedText.isBlank()) 0 else updatedText.split(" ").filter { it.isNotBlank() }.size
        
        state = state.copy(
            text = updatedText,
            wordCount = newWordCount,
            appliedSuggestions = state.suggestions.indices.toSet()
        )
    }

    private fun generateSuggestions() {
        if (state.text.isBlank()) {
            state = state.copy(suggestions = emptyList(), appliedSuggestions = emptySet())
            return
        }

        state = state.copy(isLoading = true)

        val mockSuggestions = generateMockSuggestions(state.text)
        
        state = state.copy(
            suggestions = mockSuggestions,
            appliedSuggestions = emptySet(),
            isLoading = false
        )
    }

    private fun clearError() {
        state = state.copy(error = null)
    }
    
    private fun processWordLevel(word: String, startIndex: Int) {
        wordProcessingJob?.cancel()
        
        state = state.copy(
            isProcessingWord = true,
            lastProcessedWordIndex = startIndex
        )
        
        wordProcessingJob = viewModelScope.launch {
            try {
                if (!llmInterface.isModelLoaded()) {
                    state = state.copy(isProcessingWord = false)
                    return@launch
                }
                
                val prompt = createWordLevelPrompt(word)
                var response = ""
                
                llmInterface.generateText(prompt).collect { chunk ->
                    response += chunk
                }
                
                val suggestions = parseWordLevelResponse(response, word, startIndex)
                updateSuggestionsFromWordLevel(suggestions)
                
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error processing word: ${e.message}",
                    isProcessingWord = false
                )
            } finally {
                state = state.copy(isProcessingWord = false)
            }
        }
    }
    
    private fun processSentenceLevel(sentence: String, startIndex: Int) {
        sentenceProcessingJob?.cancel()
        
        state = state.copy(
            isProcessingSentence = true,
            lastProcessedSentenceIndex = startIndex
        )
        
        sentenceProcessingJob = viewModelScope.launch {
            try {
                if (!llmInterface.isModelLoaded()) {
                    state = state.copy(isProcessingSentence = false)
                    return@launch
                }
                
                val prompt = createSentenceLevelPrompt(sentence)
                var response = ""
                
                llmInterface.generateText(prompt).collect { chunk ->
                    response += chunk
                }
                
                val suggestions = parseSentenceLevelResponse(response, sentence, startIndex)
                updateSuggestionsFromSentenceLevel(suggestions)
                
            } catch (e: Exception) {
                state = state.copy(
                    error = "Error processing sentence: ${e.message}",
                    isProcessingSentence = false
                )
            } finally {
                state = state.copy(isProcessingSentence = false)
            }
        }
    }

    private fun generateMockSuggestions(text: String): List<WritingSuggestion> {
        val suggestions = mutableListOf<WritingSuggestion>()
        
        val grammarPatterns = mapOf(
            "have many" to "has many",
            "needs to be fix" to "need to be fixed",
            "was wrote" to "was written",
            "are went" to "have gone"
        )
        
        val stylePatterns = mapOf(
            "since last month" to "for the past month",
            "very good" to "excellent",
            "a lot of" to "many",
            "kind of" to "somewhat"
        )
        
        grammarPatterns.forEach { (original, replacement) ->
            val index = text.indexOf(original, ignoreCase = true)
            if (index != -1) {
                suggestions.add(
                    WritingSuggestion(
                        original = original,
                        suggestion = replacement,
                        startIndex = index,
                        endIndex = index + original.length,
                        type = SuggestionType.GRAMMAR
                    )
                )
            }
        }
        
        stylePatterns.forEach { (original, replacement) ->
            val index = text.indexOf(original, ignoreCase = true)
            if (index != -1) {
                suggestions.add(
                    WritingSuggestion(
                        original = original,
                        suggestion = replacement,
                        startIndex = index,
                        endIndex = index + original.length,
                        type = SuggestionType.STYLE
                    )
                )
            }
        }
        
        return suggestions.sortedBy { it.startIndex }
    }
    
    private fun createWordLevelPrompt(word: String): String {
        return """
            Analyze the word "$word" for spelling mistakes and vocabulary improvements.
            
            Instructions:
            - Check if the word is spelled correctly
            - If misspelled, provide the correct spelling
            - Suggest a better vocabulary word if applicable (more formal, precise, or impactful)
            - Focus only on spelling and vocabulary, not grammar or sentence structure
            
            Response format:
            SPELLING: [correct/incorrect - if incorrect, provide correction]
            VOCABULARY: [suggest better word if applicable, otherwise "none"]
            
            Word to analyze: "$word"
        """.trimIndent()
    }
    
    private fun createSentenceLevelPrompt(sentence: String): String {
        return """
            Analyze this sentence for grammatical errors and sentence formation improvements.
            
            Instructions:
            - Check for grammatical mistakes (subject-verb agreement, tense consistency, etc.)
            - Identify sentence structure issues
            - Suggest improvements for clarity and flow
            - Focus on grammar and sentence formation, not vocabulary or spelling
            
            Response format:
            GRAMMAR: [list any grammatical errors with corrections]
            STRUCTURE: [suggest structural improvements if needed, otherwise "none"]
            
            Sentence to analyze: "$sentence"
        """.trimIndent()
    }
    
    private fun parseWordLevelResponse(response: String, originalWord: String, startIndex: Int): List<WritingSuggestion> {
        val suggestions = mutableListOf<WritingSuggestion>()
        
        try {
            val lines = response.lines()
            
            // Parse spelling correction
            val spellingLine = lines.find { it.startsWith("SPELLING:") }?.substringAfter("SPELLING:")?.trim()
            if (spellingLine?.contains("incorrect") == true) {
                val correction = spellingLine.substringAfter("-").trim()
                if (correction.isNotBlank() && correction != originalWord) {
                    suggestions.add(
                        WritingSuggestion(
                            original = originalWord,
                            suggestion = correction,
                            startIndex = startIndex,
                            endIndex = startIndex + originalWord.length,
                            type = SuggestionType.GRAMMAR
                        )
                    )
                }
            }
            
            // Parse vocabulary suggestion
            val vocabLine = lines.find { it.startsWith("VOCABULARY:") }?.substringAfter("VOCABULARY:")?.trim()
            if (vocabLine != null && vocabLine != "none" && vocabLine.isNotBlank() && vocabLine != originalWord) {
                suggestions.add(
                    WritingSuggestion(
                        original = originalWord,
                        suggestion = vocabLine,
                        startIndex = startIndex,
                        endIndex = startIndex + originalWord.length,
                        type = SuggestionType.STYLE
                    )
                )
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
        
        return suggestions
    }
    
    private fun parseSentenceLevelResponse(response: String, originalSentence: String, startIndex: Int): List<WritingSuggestion> {
        val suggestions = mutableListOf<WritingSuggestion>()
        
        try {
            val lines = response.lines()
            
            // Parse grammar corrections
            val grammarLine = lines.find { it.startsWith("GRAMMAR:") }?.substringAfter("GRAMMAR:")?.trim()
            if (grammarLine != null && grammarLine.isNotBlank() && !grammarLine.contains("none")) {
                // Extract correction if available
                if (grammarLine.contains("should be") || grammarLine.contains("correct:")) {
                    val correction = extractCorrection(grammarLine)
                    if (correction.isNotBlank() && correction != originalSentence) {
                        suggestions.add(
                            WritingSuggestion(
                                original = originalSentence,
                                suggestion = correction,
                                startIndex = startIndex,
                                endIndex = startIndex + originalSentence.length,
                                type = SuggestionType.GRAMMAR
                            )
                        )
                    }
                }
            }
            
            // Parse structure improvements
            val structureLine = lines.find { it.startsWith("STRUCTURE:") }?.substringAfter("STRUCTURE:")?.trim()
            if (structureLine != null && structureLine != "none" && structureLine.isNotBlank()) {
                val improvement = extractImprovement(structureLine)
                if (improvement.isNotBlank() && improvement != originalSentence) {
                    suggestions.add(
                        WritingSuggestion(
                            original = originalSentence,
                            suggestion = improvement,
                            startIndex = startIndex,
                            endIndex = startIndex + originalSentence.length,
                            type = SuggestionType.CLARITY
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
        
        return suggestions
    }
    
    private fun extractCorrection(grammarText: String): String {
        return when {
            grammarText.contains("should be:") -> grammarText.substringAfter("should be:").trim()
            grammarText.contains("correct:") -> grammarText.substringAfter("correct:").trim()
            grammarText.contains("→") -> grammarText.substringAfter("→").trim()
            else -> ""
        }
    }
    
    private fun extractImprovement(structureText: String): String {
        return when {
            structureText.contains("suggest:") -> structureText.substringAfter("suggest:").trim()
            structureText.contains("improvement:") -> structureText.substringAfter("improvement:").trim()
            structureText.contains("→") -> structureText.substringAfter("→").trim()
            else -> structureText.trim()
        }
    }
    
    private fun updateSuggestionsFromWordLevel(newSuggestions: List<WritingSuggestion>) {
        val currentSuggestions = state.suggestions.toMutableList()
        
        // Remove old suggestions for the same word position
        currentSuggestions.removeAll { existing ->
            newSuggestions.any { new ->
                existing.startIndex == new.startIndex && existing.endIndex == new.endIndex
            }
        }
        
        // Add new suggestions
        currentSuggestions.addAll(newSuggestions)
        
        state = state.copy(
            suggestions = currentSuggestions.sortedBy { it.startIndex },
            appliedSuggestions = emptySet() // Reset applied suggestions when new ones come in
        )
    }
    
    private fun updateSuggestionsFromSentenceLevel(newSuggestions: List<WritingSuggestion>) {
        val currentSuggestions = state.suggestions.toMutableList()
        
        // Remove old suggestions for the same sentence position
        currentSuggestions.removeAll { existing ->
            newSuggestions.any { new ->
                existing.startIndex >= new.startIndex && existing.endIndex <= new.endIndex
            }
        }
        
        // Add new suggestions
        currentSuggestions.addAll(newSuggestions)
        
        state = state.copy(
            suggestions = currentSuggestions.sortedBy { it.startIndex },
            appliedSuggestions = emptySet() // Reset applied suggestions when new ones come in
        )
    }
    
    fun onDestroy() {
        wordProcessingJob?.cancel()
        sentenceProcessingJob?.cancel()
        viewModelScope.cancel()
    }
}