package com.offline.english.ai.eng.assistant.ai

/**
 * Platform-specific LLM provider
 */
expect object LLMProvider {
    fun create(): LLMInterface
    fun getInstance(): LLMInterface
}