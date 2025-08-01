package com.offline.english.ai.eng.assistant.ai

import kotlinx.coroutines.flow.Flow

/**
 * Common interface for Large Language Model functionality across platforms
 */
interface LLMInterface {
    
    /**
     * Initialize the AI backend
     */
    suspend fun initialize(): Boolean
    
    /**
     * Load a model from the specified path or resource
     */
    suspend fun loadModel(modelPath: String): Boolean
    
    /**
     * Generate text completion for the given prompt
     */
    fun generateText(prompt: String): Flow<String>
    
    /**
     * Check if a model is currently loaded and ready
     */
    fun isModelLoaded(): Boolean
    
    /**
     * Unload the current model and free resources
     */
    suspend fun unloadModel()
    
    /**
     * Get system and model information
     */
    fun getSystemInfo(): String
}