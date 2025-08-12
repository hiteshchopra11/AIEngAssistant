package com.ai.english.assistant

data class TextBoundaryEvent(
    val type: BoundaryType,
    val text: String,
    val extractedPart: String,
    val position: Int,
    val wasBackspaceInvolved: Boolean = false
)

enum class BoundaryType {
    WORD_COMPLETED,
    SENTENCE_COMPLETED
}

object TextProcessor {
    
    private val wordBoundaryChars = setOf(' ', '\t', '\n')
    private val sentenceBoundaryChars = setOf('.', '!', '?', ',', ';', ':')
    private val strongSentenceBoundaryChars = setOf('.', '!', '?')
    
    // Track recent backspace activity to prevent processing
    private var lastBackspaceEvent = 0
    private var backspaceEventCounter = 0
    private val BACKSPACE_COOLDOWN_EVENTS = 3 // Skip processing for 3 text change events after backspace
    
    // Track processed sentences to prevent duplicates
    private val processedSentences = mutableSetOf<String>()
    private var lastProcessedTextLength = 0
    
    fun markBackspaceOccurred() {
        lastBackspaceEvent = backspaceEventCounter
    }
    
    private fun wasRecentBackspace(): Boolean {
        return (backspaceEventCounter - lastBackspaceEvent) < BACKSPACE_COOLDOWN_EVENTS
    }
    
    private fun incrementEventCounter() {
        backspaceEventCounter++
    }
    
    // Track apply/suggestion events to prevent immediate reprocessing
    fun markApplyOccurred() {
        lastBackspaceEvent = backspaceEventCounter
    }
    
    // Clear processed sentences when text changes significantly
    fun clearProcessedSentences() {
        processedSentences.clear()
    }
    
    fun detectBoundaryEvents(previousText: String, currentText: String): List<TextBoundaryEvent> {
        val events = mutableListOf<TextBoundaryEvent>()
        
        // Increment event counter for every call
        incrementEventCounter()
        
        // Only process if text was added (not deleted)
        if (currentText.length <= previousText.length) {
            // If text was deleted, mark it as potential backspace
            if (currentText.length < previousText.length) {
                markBackspaceOccurred()
            }
            return events
        }
        
        val addedText = currentText.substring(previousText.length)
        val lastAddedChar = addedText.lastOrNull()
        val wasBackspaceInvolved = wasRecentBackspace()
        
        when {
            // Word completion detected (space added)
            lastAddedChar in wordBoundaryChars -> {
                val completedWord = extractCompletedWord(currentText)
                if (completedWord.isNotBlank()) {
                    events.add(TextBoundaryEvent(
                        type = BoundaryType.WORD_COMPLETED,
                        text = currentText,
                        extractedPart = completedWord,
                        position = findWordPosition(currentText, completedWord),
                        wasBackspaceInvolved = wasBackspaceInvolved
                    ))
                }
            }
            
            // Sentence completion detected (punctuation added)
            lastAddedChar in sentenceBoundaryChars -> {
                val completedSentence = extractCompletedSentence(currentText)
                if (completedSentence.isNotBlank() && !processedSentences.contains(completedSentence.trim())) {
                    // Mark this sentence as processed
                    processedSentences.add(completedSentence.trim())
                    
                    events.add(TextBoundaryEvent(
                        type = BoundaryType.SENTENCE_COMPLETED,
                        text = currentText,
                        extractedPart = completedSentence,
                        position = findSentencePosition(currentText, completedSentence),
                        wasBackspaceInvolved = wasBackspaceInvolved
                    ))
                }
            }
        }
        
        return events
    }
    
    private fun extractCompletedWord(text: String): String {
        // Remove the trailing boundary character and extract the last word
        val textWithoutTrailing = text.trimEnd { it in wordBoundaryChars }
        val words = textWithoutTrailing.split(Regex("\\s+"))
        return if (words.isNotEmpty()) words.last().trim() else ""
    }
    
    fun extractCompletedSentence(text: String): String {
        if (text.isBlank()) return ""
        
        // Find the last punctuation mark that indicates sentence completion
        val lastPunctuationIndex = text.indexOfLast { it in sentenceBoundaryChars }
        if (lastPunctuationIndex == -1) return ""
        
        // Extract text up to and including the punctuation mark
        val textUpToPunctuation = text.substring(0, lastPunctuationIndex + 1).trim()
        
        // Find the start of the current sentence (after previous punctuation or from beginning)
        var sentenceStart = 0
        for (i in textUpToPunctuation.lastIndex - 1 downTo 0) {
            if (textUpToPunctuation[i] in sentenceBoundaryChars) {
                sentenceStart = i + 1
                break
            }
        }
        
        // Extract the completed sentence
        val completedSentence = textUpToPunctuation.substring(sentenceStart).trim()
        
        // Ensure it's a meaningful sentence (has at least 3 words)
        val wordCount = completedSentence.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        return if (wordCount >= 3) completedSentence else ""
    }

    // Returns the cumulative sentence fragment from the last strong boundary (.!?),
    // up to the most recent punctuation (which may be a comma/semicolon/colon).
    fun extractCumulativeSentenceSoFar(text: String): String {
        if (text.isBlank()) return ""

        val lastAnyPunctuationIndex = text.indexOfLast { it in sentenceBoundaryChars }
        if (lastAnyPunctuationIndex == -1) return ""

        var strongStart = 0
        for (i in lastAnyPunctuationIndex - 1 downTo 0) {
            if (text[i] in strongSentenceBoundaryChars) {
                strongStart = i + 1
                break
            }
        }

        return text.substring(strongStart, lastAnyPunctuationIndex + 1).trim()
    }

    // Returns cumulative text from the start up to the last punctuation encountered.
    // Useful when the user wants context from the beginning: e.g.,
    // "My name is Hitesh Chopra, I am good boy of ," → returns the whole string up to the trailing comma.
    fun extractCumulativeFromStart(text: String): String {
        if (text.isBlank()) return ""
        val lastAnyPunctuationIndex = text.indexOfLast { it in sentenceBoundaryChars }
        if (lastAnyPunctuationIndex == -1) return text.trim()
        return text.substring(0, lastAnyPunctuationIndex + 1).trim()
    }
    
    private fun findWordPosition(text: String, word: String): Int {
        val lastIndex = text.lastIndexOf(word)
        return if (lastIndex >= 0) lastIndex else 0
    }
    
    private fun findSentencePosition(text: String, sentence: String): Int {
        val index = text.indexOf(sentence)
        return if (index >= 0) index else 0
    }
    
    fun isValidWord(word: String): Boolean {
        return word.isNotBlank() && 
               word.length >= 1 && 
               word.all { it.isLetter() || it == '\'' || it == '-' }
    }
    
    fun isValidSentence(sentence: String): Boolean {
        return sentence.isNotBlank() && 
               sentence.trim().split("\\s+".toRegex()).size >= 2
    }
}