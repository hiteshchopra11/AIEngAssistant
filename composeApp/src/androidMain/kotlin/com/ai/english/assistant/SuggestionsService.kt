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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SuggestionsService : SuggestionContract {

    private val serviceJob: Job = SupervisorJob()
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val _grammarSuggestions = MutableStateFlow<List<GrammarSuggestionData>>(emptyList())
    override val grammarSuggestions: StateFlow<List<GrammarSuggestionData>> = _grammarSuggestions

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    private val _streamingSuggestions = MutableSharedFlow<GrammarSuggestionData>()
    override val streamingSuggestions: Flow<GrammarSuggestionData> = _streamingSuggestions.asSharedFlow()

    override fun analyzeText(fullText: String, isAdvancedMode: Boolean) {
        println("🔔 analyzeText called. length=${fullText.length}, advancedMode=$isAdvancedMode")
        if (isAdvancedMode) {
            performAdvancedAnalysis(fullText)
        } else {
            checkSentenceStructureAndStyle(fullText, false)
        }
    }

    override fun acceptSuggestion(suggestion: GrammarSuggestionData) {
        _grammarSuggestions.value = _grammarSuggestions.value.filterNot { it == suggestion }
    }

    override fun rejectSuggestion(suggestion: GrammarSuggestionData) {
        _grammarSuggestions.value = _grammarSuggestions.value.filterNot { it == suggestion }
    }

    private fun performAdvancedAnalysis(fullText: String) {
        ioScope.launch {
            _isLoading.value = true
            _grammarSuggestions.value = emptyList()

            try {
                println("🔬 Starting ADVANCED multi-iteration accuracy-optimized analysis")

                // Use a compatible model that works with streaming
                val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.0-flash-exp") // Use a more compatible model

                val allSuggestions = mutableMapOf<String, GrammarSuggestionData>()

                // ITERATION 1: Comprehensive multi-pass analysis
                println("🔄 Starting Iteration 1: Comprehensive Analysis")
                val iteration1Results = performComprehensiveAnalysisIteration(fullText, generativeModel, 1)
                
                // Add results from iteration 1
                iteration1Results.forEach { suggestion ->
                    val key = "${suggestion.original}|${suggestion.suggestion}"
                    allSuggestions[key] = suggestion
                    launch {
                        _streamingSuggestions.emit(suggestion)
                        println("✨ Iteration 1 suggestion: ${suggestion.original} → ${suggestion.suggestion}")
                    }
                }

                // ITERATION 2: Focus on missed issues and refinement
                println("🔄 Starting Iteration 2: Refinement Pass")
                val iteration2Results = performRefinementIteration(fullText, allSuggestions.values.toList(), generativeModel)
                
                // Add new results from iteration 2
                iteration2Results.forEach { suggestion ->
                    val key = "${suggestion.original}|${suggestion.suggestion}"
                    if (!allSuggestions.containsKey(key)) {
                        allSuggestions[key] = suggestion
                        launch {
                            _streamingSuggestions.emit(suggestion)
                            println("✨ Iteration 2 suggestion: ${suggestion.original} → ${suggestion.suggestion}")
                        }
                    }
                }

                // ITERATION 3: Final validation and quality assurance
                println("🔄 Starting Iteration 3: Quality Assurance")
                val finalValidatedSuggestions = performFinalValidation(fullText, allSuggestions.values.toList(), generativeModel)

                // Sort by confidence and quality
                val sortedSuggestions = finalValidatedSuggestions
                    .sortedWith(compareByDescending<GrammarSuggestionData> { it.confidence }
                        .thenBy { it.original.length }) // Prefer shorter, more focused corrections
                    .take(200) // Allow even more suggestions in advanced mode

                _grammarSuggestions.value = sortedSuggestions
                println("✅ Advanced multi-iteration analysis complete. Total high-quality suggestions: ${sortedSuggestions.size}")

            } catch (e: Exception) {
                println("❌ Advanced Analysis Error: ${e.message}")
                e.printStackTrace()
                _grammarSuggestions.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ITERATION 1: Comprehensive analysis using non-streaming approach for compatibility
    private suspend fun performComprehensiveAnalysisIteration(
        fullText: String, 
        model: com.google.firebase.ai.GenerativeModel,
        iterationNumber: Int
    ): List<GrammarSuggestionData> {
        val allSuggestions = mutableListOf<GrammarSuggestionData>()
        
        // Perform multiple specialized analyses sequentially for extreme accuracy
        val analysisTypes = listOf(
            "Grammar and Syntax" to createGrammarAnalysisPrompt(fullText),
            "Word Choice and Vocabulary" to createWordChoicePrompt(fullText),
            "Punctuation and Mechanics" to createPunctuationPrompt(fullText),
            "Style and Clarity" to createStylePrompt(fullText),
            "Coherence and Flow" to createCoherencePrompt(fullText)
        )

        for ((analysisType, prompt) in analysisTypes) {
            try {
                println("🎯 Iteration $iterationNumber: Analyzing $analysisType")
                val response = model.generateContent(prompt)
                val suggestions = parseAdvancedResponse(response.text ?: "", analysisType)
                allSuggestions.addAll(suggestions)
                println("📊 $analysisType found ${suggestions.size} suggestions")
            } catch (e: Exception) {
                println("⚠️ Error in $analysisType analysis: ${e.message}")
            }
        }

        return allSuggestions
    }

    // ITERATION 2: Refinement pass to catch missed issues
    private suspend fun performRefinementIteration(
        fullText: String,
        existingSuggestions: List<GrammarSuggestionData>,
        model: com.google.firebase.ai.GenerativeModel
    ): List<GrammarSuggestionData> {
        val existingCorrections = existingSuggestions.map { "${it.original} → ${it.suggestion}" }
        
        val refinementPrompt = """
            This is a REFINEMENT pass. You are a meticulous editor doing a second review.
            
            Original text:
            "$fullText"
            
            Already identified corrections:
            ${existingCorrections.joinToString("\n")}
            
            Your task: Find ANY additional errors that were missed. Look for:
            - Subtle grammar mistakes in complex sentences
            - Inconsistent tenses or perspectives
            - Missing articles (a, an, the)
            - Subject-verb disagreement in compound subjects
            - Dangling or misplaced modifiers
            - Comma splice errors
            - Semicolon usage errors
            - Any other errors not already found
            
            Output format: "original"|"correction"|category|reason|confidence_score
            Only output NEW errors not already found. If no additional errors, return: NONE
        """.trimIndent()

        return try {
            val response = model.generateContent(refinementPrompt)
            parseAdvancedResponse(response.text ?: "", "Refinement")
        } catch (e: Exception) {
            println("⚠️ Error in refinement iteration: ${e.message}")
            emptyList()
        }
    }

    // ITERATION 3: Final validation and quality assurance
    private suspend fun performFinalValidation(
        fullText: String,
        suggestions: List<GrammarSuggestionData>,
        model: com.google.firebase.ai.GenerativeModel
    ): List<GrammarSuggestionData> {
        if (suggestions.isEmpty()) return suggestions

        val validationPrompt = """
            You are a meticulous, world-class English proofreader acting as a quality control expert. Your task is to review proposed corrections and REJECT any that are not 100% grammatically perfect and necessary.

            Original text:
            "$fullText"

            Proposed corrections to validate:
            ${suggestions.take(100).joinToString("\n") { "${it.original} → ${it.suggestion} (${it.category}: ${it.explanation})" }}

            For each correction, you must strictly evaluate:
            1.  Is the original text definitively incorrect according to standard English grammar rules? (Reject if it's a stylistic choice).
            2.  Is the proposed correction absolutely, unequivocally correct? (e.g., "fixed" not "fixeded", "doesn't have" not "don't has").
            3.  Does the correction preserve the original meaning?
            4.  Is the correction significant? (Reject trivial changes).

            Your output MUST ONLY contain corrections that pass ALL of these criteria.
            Your output format MUST be:
            "original"|"correction"|category|reason|confidence_score

            CRITICAL: If a proposed correction contains ANY grammatical error (e.g., "fixeded", "don't has"), you MUST reject it. Do not attempt to fix the correction. Simply discard it.
            Only output the validated, necessary, and PERFECT corrections.
        """.trimIndent()

        return try {
            val response = model.generateContent(validationPrompt)
            val validatedSuggestions = parseAdvancedResponse(response.text ?: "", "Validation")
            println("✅ Validation: ${validatedSuggestions.size} suggestions approved out of ${suggestions.size}")
            validatedSuggestions
        } catch (e: Exception) {
            println("⚠️ Error in validation: ${e.message}, returning original suggestions")
            suggestions
        }
    }

    // Helper functions to create specialized prompts
    private fun createGrammarAnalysisPrompt(text: String) = """
        You are an expert English linguist. Find ALL grammar errors in this text with extreme precision.
        
        Text: "$text"
        
        Focus on: subject-verb agreement, pronoun-antecedent agreement, verb tenses, sentence fragments, run-on sentences, comma splices, parallel structure, dangling modifiers.
        
        Output format: "original_error"|"correction"|Grammar|detailed_explanation|confidence_0.0-1.0
        Only output actual grammatical errors. If none found, return: NONE
    """.trimIndent()

    private fun createWordChoicePrompt(text: String) = """
        Find word choice errors and vocabulary issues in this text.
        
        Text: "$text"
        
        Focus on: wrong words, confused words (affect/effect, its/it's), malapropisms, imprecise vocabulary, inappropriate register.
        
        Output format: "wrong_word"|"correct_word"|Word_Choice|explanation|confidence_0.0-1.0
        If no issues, return: NONE
    """.trimIndent()

    private fun createPunctuationPrompt(text: String) = """
        Find punctuation and capitalization errors in this text.
        
        Text: "$text"
        
        Focus on: missing/misused commas, apostrophe errors, capitalization, quotation marks, semicolons, colons.
        
        Output format: "original_error"|"correction"|Punctuation|explanation|confidence_0.0-1.0
        The "original_error" should be a short phrase, not a full sentence.
        If no errors, return: NONE
    """.trimIndent()

    private fun createStylePrompt(text: String) = """
        Find clarity and style issues that significantly impact readability.
        
        Text: "$text"
        
        Focus on: unclear pronouns, wordy constructions, unclear antecedents, unnecessarily complex phrasing.
        
        Output format: "unclear_text"|"clearer_text"|Clarity|explanation|confidence_0.0-1.0
        Only suggest changes that meaningfully improve clarity. If none needed, return: NONE
    """.trimIndent()

    private fun createCoherencePrompt(text: String) = """
        Find coherence and flow issues in this text.
        
        Text: "$text"
        
        Focus on: missing transitions, logical gaps, inconsistent perspective, unclear references.
        
        Output format: "original_text"|"improved_text"|Coherence|explanation|confidence_0.0-1.0
        Focus on micro-edits, not paragraph restructuring. If no issues, return: NONE
    """.trimIndent()

    // Parse non-streaming response
    private fun parseAdvancedResponse(response: String, category: String): List<GrammarSuggestionData> {
        val suggestions = mutableListOf<GrammarSuggestionData>()
        
        response.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotBlank() && !trimmedLine.equals("NONE", ignoreCase = true)) {
                parseAdvancedLine(trimmedLine, category)?.let { suggestions.add(it) }
            }
        }
        
        return suggestions
    }


    private fun checkSentenceStructureAndStyle(fullText: String, isAdvancedMode: Boolean = false) {
        ioScope.launch {
            _isLoading.value = true
            _grammarSuggestions.value = emptyList() // Clear previous suggestions

            try {
                val analysisType = if (isAdvancedMode) "advanced multi-iteration" else "fast single-pass"
                println("📝 Streaming text analysis for micro-edits using $analysisType mode")

                val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel("gemini-2.0-flash-lite") // Use better model for standard mode too

                // Standard mode prompt (unchanged from original)
                val prompt = """
                    You are an English writing assistant. Read the text and propose max up to 100 micro-edits.
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
                    "don't likes"|"doesn't like"|Verb_Tense|Fix agreement after subject
                    "go"|"to go"|Grammar|Use infinitive after 'like'
                    "because too much noisy"|"because it is too noisy"|Clarity|Complete clause with copula

                    If no edits are needed, return: NONE
                """.trimIndent()

                println("📝 Starting streaming analysis...")
                val allSuggestions = mutableListOf<GrammarSuggestionData>()
                var accumulatedText = ""

                // Stream the response and parse suggestions as they come
                generativeModel.generateContentStream(prompt).collect { chunk ->
                    val chunkText = chunk.text.orEmpty()
                    if (chunkText.isNotBlank()) {
                        println("📡 Received chunk: $chunkText")
                        accumulatedText += chunkText

                        // Process complete lines immediately as they become available
                        while (accumulatedText.contains("\n")) {
                            val lineBreakIndex = accumulatedText.indexOf("\n")
                            val completeLine = accumulatedText.substring(0, lineBreakIndex).trim()
                            accumulatedText = accumulatedText.substring(lineBreakIndex + 1)

                            // Parse this single complete line
                            if (completeLine.isNotBlank() && !completeLine.equals("NONE", ignoreCase = true)) {
                                val suggestion = parseSingleLine(completeLine)
                                if (suggestion != null && !allSuggestions.contains(suggestion)) {
                                    allSuggestions.add(suggestion)

                                    // Emit individual suggestion immediately
                                    launch {
                                        _streamingSuggestions.emit(suggestion)
                                        println("✨ Streamed suggestion: ${suggestion.original} → ${suggestion.suggestion}")
                                    }
                                }
                            }
                        }
                    }
                }

                // Process any remaining text
                if (accumulatedText.isNotBlank()) {
                    val suggestion = parseSingleLine(accumulatedText.trim())
                    if (suggestion != null && !allSuggestions.contains(suggestion)) {
                        allSuggestions.add(suggestion)
                        launch { _streamingSuggestions.emit(suggestion) }
                        println("✨ Final suggestion: ${suggestion.original} → ${suggestion.suggestion}")
                    }
                }

                // Update final StateFlow with all suggestions for any late subscribers
                _grammarSuggestions.value = allSuggestions.toList()
                println("✅ Streaming complete. Total suggestions: ${allSuggestions.size}")

            } catch (e: Exception) {
                println("❌ Firebase AI Streaming Error: ${e.message}")
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

    // Parse a single line into a suggestion (standard mode)
    private fun parseSingleLine(line: String): GrammarSuggestionData? {
        val parts = line.split("|")
        if (parts.size >= 2) {
            val original = parts[0].trim().removeSurrounding("\"")
            val suggestion = parts.getOrNull(1)?.trim()?.removeSurrounding("\"") ?: ""
            val category = parts.getOrNull(2)?.trim().orEmpty()
            val reason = parts.getOrNull(3)?.trim().orEmpty()
            if (original.isNotBlank() && suggestion.isNotBlank()) {
                val type = when (category.lowercase()) {
                    "spelling" -> SuggestionType.SPELLING
                    "word_choice", "vocabulary" -> SuggestionType.STYLE
                    "clarity" -> SuggestionType.CLARITY
                    "punctuation", "capitalization", "verb_tense", "article_usage", "grammar" -> SuggestionType.GRAMMAR_SENTENCE
                    else -> SuggestionType.GRAMMAR_SENTENCE
                }
                return GrammarSuggestionData(
                    original = original,
                    suggestion = suggestion,
                    type = type,
                    confidence = 0.9f,
                    timestamp = System.currentTimeMillis(),
                    category = category.ifBlank { "Grammar" },
                    explanation = reason
                )
            }
        }
        return null
    }

    // Parse a line with confidence score (advanced mode)
    private fun parseAdvancedLine(line: String, defaultCategory: String): GrammarSuggestionData? {
        try {
            val parts = line.split("|")
            println("🔍 Parsing parts: ${parts.joinToString(" | ")}")
            
            if (parts.size >= 2) {
                val original = parts[0].trim().removeSurrounding("\"")
                val suggestion = parts[1].trim().removeSurrounding("\"")
                val category = parts.getOrNull(2)?.trim() ?: defaultCategory
                val explanation = parts.getOrNull(3)?.trim() ?: ""
                
                // Handle confidence - could be in part 4 or embedded in explanation
                var confidence = 0.85f
                var cleanExplanation = explanation
                
                if (parts.size >= 5) {
                    // Confidence is in its own field
                    confidence = parts[4].trim().toFloatOrNull() ?: 0.85f
                } else if (explanation.contains("confidence")) {
                    // Try to extract confidence from explanation
                    val confidenceRegex = Regex("confidence[_\\s]*([0-9.]+)")
                    val match = confidenceRegex.find(explanation.lowercase())
                    match?.let {
                        confidence = it.groupValues[1].toFloatOrNull() ?: 0.85f
                        cleanExplanation = explanation.replace(confidenceRegex, "").trim()
                    }
                }
                
                // Ensure confidence is in valid range
                confidence = confidence.coerceIn(0.0f, 1.0f)

                if (original.isNotBlank() && suggestion.isNotBlank()) {
                    val type = when (category.lowercase()) {
                        "spelling" -> SuggestionType.SPELLING
                        "word_choice", "vocabulary" -> SuggestionType.STYLE
                        "clarity" -> SuggestionType.CLARITY
                        "coherence", "flow", "punctuation", "grammar" -> SuggestionType.GRAMMAR_SENTENCE
                        else -> SuggestionType.GRAMMAR_SENTENCE
                    }

                    return GrammarSuggestionData(
                        original = original,
                        suggestion = suggestion,
                        type = type,
                        confidence = confidence,
                        timestamp = System.currentTimeMillis(),
                        category = category,
                        explanation = cleanExplanation
                    )
                }
            }
        } catch (e: Exception) {
            println("❌ Exception parsing line '$line': ${e.message}")
        }
        return null
    }
}