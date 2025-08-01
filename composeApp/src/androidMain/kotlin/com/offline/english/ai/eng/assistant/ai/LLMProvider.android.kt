package com.offline.english.ai.eng.assistant.ai

/**
 * Android implementation of LLMProvider
 */
actual object LLMProvider {
    private val llmInstance: LLMInterface by lazy { AndroidLLMImplementation() }
    
    actual fun create(): LLMInterface {
        return AndroidLLMImplementation()
    }
    
    actual fun getInstance(): LLMInterface {
        return llmInstance
    }
}