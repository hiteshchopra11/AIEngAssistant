package com.offline.english.ai.eng.assistant

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File

class GemmaInference(private val context: Context) {

    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private val TAG = "GemmaInference"

    fun initialize(): Boolean {
        return try {
            // Check if model file exists in assets or external storage
            val modelPath = getModelPath()
            if (modelPath == null) {
                Log.w(TAG, "No model file found. Please place a .task model file in assets directory.")
                return false
            }

            val taskOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .build()

            llmInference = LlmInference.createFromOptions(context, taskOptions)
            isInitialized = true
            Log.i(TAG, "MediaPipe LLM initialized successfully with model: $modelPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaPipe LLM: ${e.message}", e)
            false
        }
    }

    private fun getModelPath(): String? {
        // First, try to find a .task file in assets
        try {
            val assetFiles = context.assets.list("") ?: emptyArray()
            Log.d(TAG, "Assets found: ${assetFiles.joinToString()}")
            val taskFile = assetFiles.find { it.endsWith(".task") }
            if (taskFile != null) {
                Log.i(TAG, "Found model in assets: $taskFile")
                return taskFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not list assets: ${e.message}")
        }

        // Then try external storage paths (following MediaPipe documentation)
        val externalPaths = listOf(
            "/data/local/tmp/llm/model_version.task",  // MediaPipe official path
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/model.task",
            "${Environment.getExternalStorageDirectory()}/models/model.task",
            "${context.getExternalFilesDir(null)}/model.task",
            "${context.filesDir}/model.task"
        )

        for (path in externalPaths) {
            Log.d(TAG, "Checking path: $path")
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    Log.i(TAG, "Found readable model at: $path (size: ${file.length()} bytes)")
                    return path
                } else {
                    Log.d(TAG, "Path not accessible: $path (exists: ${file.exists()}, readable: ${file.canRead()})")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error checking path $path: ${e.message}")
            }
        }

        Log.w(TAG, "No model file found in any location")
        return null
    }

    /** Run inference synchronously with timeout */
    fun generate(inputPrompt: String): String {
        if (!isInitialized && !initialize()) {
            return "Error: Model not initialized. Please ensure a .task model file is available."
        }
        
        return try {
            Log.d(TAG, "GENERATING (with timeout)")
            
            // Use runBlocking with timeout to prevent hanging
            runBlocking {
                withTimeout(30000) { // 30 second timeout
                    val result = withContext(Dispatchers.IO) {
                        llmInference?.generateResponse(inputPrompt)
                    }
                    Log.d(TAG, "Generated response: ${result?.take(50)}...")
                    result ?: "Error: Model returned null"
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Generation timed out after 30 seconds")
            "Error: Generation timed out. Try a shorter prompt."
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response: ${e.message}", e)
            "Error: Failed to generate response - ${e.message}"
        }
    }

    /** Run inference asynchronously */
    fun generateAsync(inputPrompt: String) {
        if (!isInitialized && !initialize()) {
            Log.e(TAG, "Cannot run async generation: Model not initialized")
            return
        }

        try {
            llmInference?.generateResponseAsync(inputPrompt)
        } catch (e: Exception) {
            Log.e(TAG, "Error in async generation: ${e.message}", e)
        }
    }

    fun isModelLoaded(): Boolean = isInitialized

    fun cleanup() {
        llmInference = null
        isInitialized = false
    }
}
