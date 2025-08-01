package com.offline.english.ai.eng.assistant

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class LlamaAndroid private constructor() {
    
    companion object {
        private const val TAG = "LlamaAndroid"
        
        @Volatile
        private var INSTANCE: LlamaAndroid? = null
        
        fun instance(): LlamaAndroid {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LlamaAndroid().also { INSTANCE = it }
            }
        }
        
        private var isNativeLibraryLoaded = false
        
        init {
            try {
                System.loadLibrary("llama-android")
                isNativeLibraryLoaded = true
                Log.i(TAG, "Native library loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                isNativeLibraryLoaded = false
                Log.w(TAG, "Native library not available - using placeholder mode", e)
            }
        }
    }
    
    private val executor = Executors.newSingleThreadExecutor()
    private var modelPtr: Long = 0
    private var contextPtr: Long = 0
    private val isLoaded = AtomicBoolean(false)
    
    // Native method declarations
    private external fun loadModel(pathToModel: String): Long
    private external fun newContext(modelPtr: Long): Long
    private external fun completionInit(contextPtr: Long, prompt: String)
    private external fun completionLoop(contextPtr: Long): String
    private external fun backendInit(): Boolean
    private external fun systemInfo(): String
    private external fun freeModel(modelPtr: Long)
    private external fun freeContext(contextPtr: Long)
    
    /**
     * Initialize the llama backend
     */
    fun initBackend(): Boolean {
        return if (isNativeLibraryLoaded) {
            try {
                val result = backendInit()
                Log.i(TAG, "Backend initialization: $result")
                result
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize backend", e)
                false
            }
        } else {
            Log.i(TAG, "Backend initialization: placeholder mode")
            true // Return true for placeholder mode
        }
    }
    
    /**
     * Load a model from the specified file path
     */
    fun load(pathToModel: String): Boolean {
        return if (isNativeLibraryLoaded) {
            try {
                if (isLoaded.get()) {
                    Log.w(TAG, "Model already loaded, unloading first")
                    unload()
                }
                
                Log.i(TAG, "Loading model from: $pathToModel")
                modelPtr = loadModel(pathToModel)
                
                if (modelPtr != 0L) {
                    contextPtr = newContext(modelPtr)
                    if (contextPtr != 0L) {
                        isLoaded.set(true)
                        Log.i(TAG, "Model loaded successfully")
                        return true
                    } else {
                        Log.e(TAG, "Failed to create context")
                        freeModel(modelPtr)
                        modelPtr = 0
                    }
                } else {
                    Log.e(TAG, "Failed to load model")
                }
                false
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading model", e)
                false
            }
        } else {
            Log.i(TAG, "Model loading: placeholder mode - simulating success")
            isLoaded.set(true)
            true
        }
    }
    
    /**
     * Generate text completion using the loaded model
     */
    fun send(message: String, formatChat: Boolean = false): Flow<String> = flow {
        if (!isLoaded.get()) {
            Log.e(TAG, "Model not loaded")
            emit("Error: Model not loaded")
            return@flow
        }
        
        if (isNativeLibraryLoaded) {
            try {
                val prompt = if (formatChat) {
                    // Simple chat formatting - can be enhanced based on model requirements
                    "User: $message\nAssistant: "
                } else {
                    message
                }
                
                Log.d(TAG, "Sending prompt: $prompt")
                completionInit(contextPtr, prompt)
                
                // Generate tokens in a loop
                var token: String
                val maxTokens = 512 // Configurable limit
                var tokenCount = 0
                
                do {
                    token = completionLoop(contextPtr)
                    if (token.isNotEmpty() && tokenCount < maxTokens) {
                        emit(token)
                        tokenCount++
                    }
                } while (token.isNotEmpty() && tokenCount < maxTokens)
                
                Log.d(TAG, "Generated $tokenCount tokens")
                
            } catch (e: Exception) {
                Log.e(TAG, "Exception during text generation", e)
                emit("Error: ${e.message}")
            }
        } else {
            // Placeholder mode - simulate text generation
            Log.d(TAG, "Text generation: placeholder mode")
            val placeholderResponse = "This is a placeholder response. The actual llama.cpp integration is not yet complete. Your message was: '$message'"
            
            // Simulate token-by-token generation
            val words = placeholderResponse.split(" ")
            for (word in words) {
                emit("$word ")
                kotlinx.coroutines.delay(100) // Simulate generation delay
            }
        }
    }
    
    /**
     * Benchmark model performance
     */
    fun bench(): String {
        return try {
            if (!isLoaded.get()) {
                "Model not loaded"
            } else {
                val sysInfo = systemInfo()
                Log.i(TAG, "System info: $sysInfo")
                sysInfo
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during benchmark", e)
            "Benchmark failed: ${e.message}"
        }
    }
    
    /**
     * Unload the model and free resources
     */
    fun unload() {
        try {
            if (contextPtr != 0L) {
                freeContext(contextPtr)
                contextPtr = 0
            }
            if (modelPtr != 0L) {
                freeModel(modelPtr)
                modelPtr = 0
            }
            isLoaded.set(false)
            Log.i(TAG, "Model unloaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Exception while unloading model", e)
        }
    }
    
    /**
     * Check if model is loaded
     */
    fun isModelLoaded(): Boolean = isLoaded.get()
    
    /**
     * Get system information
     */
    fun getSystemInfo(): String {
        return try {
            systemInfo()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system info", e)
            "System info unavailable"
        }
    }
}