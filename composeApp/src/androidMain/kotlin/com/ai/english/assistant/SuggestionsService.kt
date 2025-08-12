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

    override fun onWordCompleted(fullText: String, completedWord: String) {
        // Disabled word-level checking for better performance - focus on sentences only
    }

    override fun onSentenceCompleted(fullText: String) {
        // Button-driven analysis: treat this as analyzeFullText for simpler mental model
        println("🔔 analyzeFullText called via onSentenceCompleted. length=${fullText.length}")
        checkSentenceStructureAndStyle(fullText)
    }

    override fun acceptSuggestion(suggestion: GrammarSuggestionData) {
        _grammarSuggestions.value = _grammarSuggestions.value.filterNot { it == suggestion }
    }

    override fun rejectSuggestion(suggestion: GrammarSuggestionData) {
        _grammarSuggestions.value = _grammarSuggestions.value.filterNot { it == suggestion }
    }

    private fun checkWordSpellingAndVocab(fullText: String, word: String) {
        ioScope.launch {
            _isLoading.value = true
            try {
                println("🔍 Checking word: '$word' in context: '${fullText.takeLast(50)}'")
                
                val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.5-flash")
                    
                val prompt = "Check the word \"$word\" for spelling mistakes and better vocabulary alternatives. Context: \"${fullText.takeLast(200)}\". Return JSON: {\"hasSpellingError\": boolean, \"correctSpelling\": \"string\", \"vocabularyAlternatives\": [\"word1\", \"word2\"]}. If no issues: {\"hasSpellingError\": false, \"vocabularyAlternatives\": []}"
                
                println("📝 Sending prompt to Firebase AI...")
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text.orEmpty()
                println("🤖 Firebase AI Response: $responseText")
                
                val wordSuggestion = parseWordCheckJson(responseText, word)
                if (wordSuggestion != null) {
                    println("✅ Generated suggestion: ${wordSuggestion.original} → ${wordSuggestion.suggestion}")
                    val currentSuggestions = _grammarSuggestions.value.toMutableList()
                    currentSuggestions.add(wordSuggestion)
                    _grammarSuggestions.value = currentSuggestions
                } else {
                    println("ℹ️ No suggestions generated for word: $word")
                }
            } catch (e: Exception) {
                println("❌ Firebase AI Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
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

    private fun parseGrammarJson(raw: String): List<GrammarSuggestionData> {
        // Extremely small permissive parser to avoid adding a JSON dependency to common code.
        // We expect a JSON array of simple objects. We'll do minimal parsing best-effort.
        val safe = raw
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        if (!safe.startsWith("[") || !safe.endsWith("]")) return emptyList()

        // Split on '},{' boundaries heuristically
        val objects = safe.substring(1, safe.length - 1)
            .split(Regex("\\},\\s*\\{"))
            .mapIndexed { index, obj ->
                val normalized = when {
                    index == 0 && !obj.trim().startsWith("{") -> "{" + obj
                    index == 0 -> obj
                    else -> obj
                }.let { s -> if (!s.trim().endsWith("}")) s + "}" else s }
                normalized
            }

        val results = mutableListOf<GrammarSuggestionData>()
        for (obj in objects) {
            val original = findJsonStringValue(obj, "original").orEmpty()
            val suggestion = findJsonStringValue(obj, "suggestion").orEmpty()
            val typeStr = findJsonStringValue(obj, "type").orEmpty()
            val confidence = findJsonNumberValue(obj, "confidence") ?: 1.0

            if (original.isBlank() || suggestion.isBlank()) continue
            val type = when (typeStr.uppercase()) {
                "GRAMMAR_WORD" -> SuggestionType.GRAMMAR_WORD
                "GRAMMAR_SENTENCE" -> SuggestionType.GRAMMAR_SENTENCE
                "SPELLING" -> SuggestionType.SPELLING
                else -> SuggestionType.GRAMMAR_SENTENCE
            }
            results.add(
                GrammarSuggestionData(
                    original = original,
                    suggestion = suggestion,
                    type = type,
                    confidence = confidence.toFloat(),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return results
    }

    private fun findJsonStringValue(obj: String, key: String): String? {
        val regex = Regex("\"$key\"\\s*:\\s*\"(.*?)\"", RegexOption.DOT_MATCHES_ALL)
        return regex.find(obj)?.groupValues?.getOrNull(1)
    }

    private fun findJsonNumberValue(obj: String, key: String): Double? {
        val regex = Regex("\"$key\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)")
        return regex.find(obj)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
    }
    
    private fun parseWordCheckJson(raw: String, originalWord: String): GrammarSuggestionData? {
        val safe = raw
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        
        try {
            val hasSpellingError = findJsonBooleanValue(safe, "hasSpellingError") ?: false
            val correctSpelling = findJsonStringValue(safe, "correctSpelling")
            val vocabularyAlternatives = findJsonArrayValue(safe, "vocabularyAlternatives")
            
            return when {
                hasSpellingError && !correctSpelling.isNullOrBlank() -> {
                    GrammarSuggestionData(
                        original = originalWord,
                        suggestion = correctSpelling,
                        type = SuggestionType.SPELLING,
                        confidence = 0.9f,
                        timestamp = System.currentTimeMillis()
                    )
                }
                vocabularyAlternatives.isNotEmpty() -> {
                    // Pick the first vocabulary alternative
                    GrammarSuggestionData(
                        original = originalWord,
                        suggestion = vocabularyAlternatives.first(),
                        type = SuggestionType.GRAMMAR_WORD,
                        confidence = 0.8f,
                        timestamp = System.currentTimeMillis()
                    )
                }
                else -> null
            }
        } catch (_: Exception) {
            return null
        }
    }
    
    private fun findJsonBooleanValue(obj: String, key: String): Boolean? {
        val regex = Regex("\"$key\"\\s*:\\s*(true|false)")
        return regex.find(obj)?.groupValues?.getOrNull(1)?.toBooleanStrictOrNull()
    }
    
    private fun findJsonArrayValue(obj: String, key: String): List<String> {
        val regex = Regex("\"$key\"\\s*:\\s*\\[(.*?)\\]", RegexOption.DOT_MATCHES_ALL)
        val arrayContent = regex.find(obj)?.groupValues?.getOrNull(1) ?: return emptyList()
        
        return arrayContent.split(',')
            .mapNotNull { item ->
                val trimmed = item.trim().removeSurrounding("\"")
                if (trimmed.isNotBlank()) trimmed else null
            }
    }
    
    // Enhanced parser for format: "original|suggestion|category|explanation"
    @Suppress("UNUSED_PARAMETER")
    private fun parseOptimizedResponse(response: String, fallbackOriginal: String): GrammarSuggestionData? {
        val cleaned = response.trim()
        if (cleaned.equals("NONE", ignoreCase = true)) return null
        
        val parts = cleaned.split("|")
        if (parts.size >= 2) {
            val original = parts[0].trim().removeSurrounding("\"")
            val suggestion = parts[1].trim().removeSurrounding("\"")
            val category = if (parts.size >= 3) parts[2].trim() else "Grammar"
            val explanation = if (parts.size >= 4) parts[3].trim() else ""
            
            if (original.isNotBlank() && suggestion.isNotBlank()) {
                val type = when (category.lowercase()) {
                    "spelling" -> SuggestionType.SPELLING
                    "word_choice", "vocabulary", "clarity" -> SuggestionType.GRAMMAR_WORD
                    else -> SuggestionType.GRAMMAR_SENTENCE
                }
                
                return GrammarSuggestionData(
                    original = original,
                    suggestion = suggestion,
                    type = type,
                    confidence = 0.9f,
                    timestamp = System.currentTimeMillis(),
                    category = category,
                    explanation = explanation
                )
            }
        }
        
        return null
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
