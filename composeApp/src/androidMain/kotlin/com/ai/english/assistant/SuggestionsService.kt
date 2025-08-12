package com.ai.english.assistant

import com.ai.english.assistant.domain.GrammarSuggestionData
import com.ai.english.assistant.domain.SuggestionType

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SuggestionsService : SuggestionContract {

    private val serviceJob: Job = SupervisorJob()
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val _grammarSuggestions = MutableStateFlow<List<GrammarSuggestionData>>(emptyList())
    override val grammarSuggestions: StateFlow<List<GrammarSuggestionData>> = _grammarSuggestions

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    override fun analyzeText(fullText: String) {
        println("🔔 analyzeText called. length=${fullText.length}")
        checkSentenceStructureAndStyle(fullText)
    }

    override fun acceptSuggestion(suggestion: GrammarSuggestionData) {
        _grammarSuggestions.value = _grammarSuggestions.value.filterNot { it == suggestion }
    }

    override fun rejectSuggestion(suggestion: GrammarSuggestionData) {
        _grammarSuggestions.value = _grammarSuggestions.value.filterNot { it == suggestion }
    }

    private fun checkSentenceStructureAndStyle(fullText: String) {
        ioScope.launch {
            _isLoading.value = true
            try {
                println("📝 Checking complete text for micro-edits")
                
                val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash")
                    
                // Strongly bias toward short-span corrections; avoid full-sentence rewrites
                val prompt = """
                    You are an English writing assistant. Read the text and propose up to 5 micro-edits.
                    Each micro-edit must replace a SHORT contiguous span (word or phrase) with a corrected span.
                    Do NOT return full-sentence rewrites. Spans must appear exactly in the text.

                    Text:
                    "$fullText"

                    Output: one suggestion per line, pipe-separated with no extra text:
                    "original_span"|"suggested_span"|category|reason
                    - Keep spans short and focused (avoid whole sentences)
                    - Categories: Grammar, Spelling, Word_Choice, Clarity, Punctuation, Capitalization, Verb_Tense, Article_Usage
                    - Reason: short bullet (no 'and')

                    Good examples:
                    "Him"|"He"|Grammar|Use subject pronoun
                    "don’t likes"|"doesn't like"|Verb_Tense|Fix agreement after subject
                    "go"|"to go"|Grammar|Use infinitive after 'like'
                    "because too much noisy"|"because it is too noisy"|Clarity|Complete clause with copula

                    If no edits are needed, return: NONE
                """.trimIndent()
                
                println("📝 Sending optimized prompt to Firebase AI...")
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text.orEmpty().trim()
                println("🤖 Firebase AI Response: $responseText")
                
                val suggestions = parseMultipleSuggestions(responseText)
                if (suggestions.isNotEmpty()) {
                    println("✅ Generated ${suggestions.size} optimized suggestions")
                    _grammarSuggestions.value = suggestions
                } else {
                    println("ℹ️ No improvements needed")
                    _grammarSuggestions.value = emptyList()
                }
            } catch (e: Exception) {
                println("❌ Firebase AI Error: ${e.message}")
                _grammarSuggestions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun clearSuggestions() {
        _grammarSuggestions.value = emptyList()
        _isLoading.value = false
    }

    override fun cleanup() {
        serviceJob.cancel()
        clearSuggestions()
    }

    // Parse multi-line pipe-separated suggestions. Each line:
    // "original"|"suggestion"|Category|Reason
    private fun parseMultipleSuggestions(response: String): List<GrammarSuggestionData> {
        val cleanedLines = response.trim().lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.equals("NONE", ignoreCase = true) }

        val results = mutableListOf<GrammarSuggestionData>()
        for (line in cleanedLines) {
            val parts = line.split("|")
            if (parts.size >= 2) {
                val original = parts[0].trim().removeSurrounding("\"")
                val suggestion = parts.getOrNull(1)?.trim()?.removeSurrounding("\"") ?: ""
                val category = parts.getOrNull(2)?.trim().orEmpty()
                val reason = parts.getOrNull(3)?.trim().orEmpty()
                if (original.isNotBlank() && suggestion.isNotBlank()) {
                    val type = when (category.lowercase()) {
                        "spelling" -> SuggestionType.SPELLING
                        "word_choice", "vocabulary", "clarity" -> SuggestionType.GRAMMAR_WORD
                        "punctuation", "capitalization", "verb_tense", "article_usage", "grammar" -> SuggestionType.GRAMMAR_SENTENCE
                        else -> SuggestionType.GRAMMAR_SENTENCE
                    }
                    results.add(
                        GrammarSuggestionData(
                            original = original,
                            suggestion = suggestion,
                            type = type,
                            confidence = 0.9f,
                            timestamp = System.currentTimeMillis(),
                            category = category.ifBlank { "Grammar" },
                            explanation = reason
                        )
                    )
                }
            }
        }
        return results
    }
}
