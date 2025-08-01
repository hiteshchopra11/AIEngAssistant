package com.offline.english.ai.eng.assistant.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * iOS stub implementation of LLMInterface (not implemented yet)
 */
class iOSLLMImplementation : LLMInterface {
    
    override suspend fun initialize(): Boolean {
        // TODO: Implement iOS LLM initialization
        return false
    }
    
    override suspend fun loadModel(modelPath: String): Boolean {
        // TODO: Implement iOS model loading
        return false
    }
    
    override fun generateText(prompt: String): Flow<String> {
        // TODO: Implement iOS text generation
        return flowOf("iOS LLM not implemented yet")
    }
    
    override fun isModelLoaded(): Boolean {
        // TODO: Implement iOS model status check
        return false
    }
    
    override suspend fun unloadModel() {
        // TODO: Implement iOS model unloading
    }
    
    override fun getSystemInfo(): String {
        // TODO: Implement iOS system info
        return "iOS system info not implemented"
    }
}

/**
 * iOS implementation of LLMProvider
 */
actual object LLMProvider {
    private val llmInstance: LLMInterface by lazy { iOSLLMImplementation() }
    
    actual fun create(): LLMInterface {
        return iOSLLMImplementation()
    }
    
    actual fun getInstance(): LLMInterface {
        return llmInstance
    }
}