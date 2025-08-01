package com.offline.english.ai.eng.assistant.ai

import com.offline.english.ai.eng.assistant.LlamaAndroid
import kotlinx.coroutines.flow.Flow

/**
 * Android implementation of LLMInterface using llama.cpp
 */
class AndroidLLMImplementation : LLMInterface {
    
    private val llamaAndroid = LlamaAndroid.instance()
    
    override suspend fun initialize(): Boolean {
        return llamaAndroid.initBackend()
    }
    
    override suspend fun loadModel(modelPath: String): Boolean {
        return llamaAndroid.load(modelPath)
    }
    
    override fun generateText(prompt: String): Flow<String> {
        return llamaAndroid.send(prompt, formatChat = true)
    }
    
    override fun isModelLoaded(): Boolean {
        return llamaAndroid.isModelLoaded()
    }
    
    override suspend fun unloadModel() {
        llamaAndroid.unload()
    }
    
    override fun getSystemInfo(): String {
        return llamaAndroid.getSystemInfo()
    }
}