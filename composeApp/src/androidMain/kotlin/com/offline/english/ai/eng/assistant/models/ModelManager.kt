package com.offline.english.ai.eng.assistant.models

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest
import kotlin.math.roundToInt

data class ModelInfo(
    val id: String,
    val name: String,
    val description: String,
    val size: Long, // Size in bytes
    val downloadUrl: String,
    val checksum: String? = null,
    val isRecommended: Boolean = false
)

data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int
)

class ModelManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelManager"
        private const val MODELS_DIR = "models"
        
        // Popular small GGUF models for mobile
        val AVAILABLE_MODELS = listOf(
            ModelInfo(
                id = "tinyllama-1.1b-chat-q4_0",
                name = "TinyLlama 1.1B Chat",
                description = "Lightweight chat model, fast inference (500MB)",
                size = 500_000_000L, // ~500MB
                downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_0.gguf",
                isRecommended = true
            ),
            ModelInfo(
                id = "phi-2-q4_0",
                name = "Microsoft Phi-2",
                description = "Small but capable model by Microsoft (1.5GB)",
                size = 1_500_000_000L, // ~1.5GB
                downloadUrl = "https://huggingface.co/microsoft/phi-2-GGUF/resolve/main/phi-2.q4_0.gguf",
                isRecommended = true
            ),
            ModelInfo(
                id = "llama-2-7b-chat-q4_0",
                name = "Llama 2 7B Chat",
                description = "High quality chat model (requires 4GB+ RAM)",
                size = 4_000_000_000L, // ~4GB
                downloadUrl = "https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGUF/resolve/main/llama-2-7b-chat.q4_0.gguf",
                isRecommended = false
            )
        )
    }
    
    private val modelsDir: File by lazy {
        File(context.filesDir, MODELS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Get list of available models for download
     */
    fun getAvailableModels(): List<ModelInfo> = AVAILABLE_MODELS
    
    /**
     * Get list of downloaded models
     */
    fun getDownloadedModels(): List<Pair<ModelInfo, File>> {
        return AVAILABLE_MODELS.mapNotNull { model ->
            val file = getModelFile(model.id)
            if (file.exists() && file.length() > 0) {
                model to file
            } else null
        }
    }
    
    /**
     * Check if a model is downloaded
     */
    fun isModelDownloaded(modelId: String): Boolean {
        return getModelFile(modelId).let { it.exists() && it.length() > 0 }
    }
    
    /**
     * Get the file path for a model
     */
    fun getModelFile(modelId: String): File {
        val model = AVAILABLE_MODELS.find { it.id == modelId }
            ?: throw IllegalArgumentException("Unknown model: $modelId")
        return File(modelsDir, "${model.id}.gguf")
    }
    
    /**
     * Download a model with progress updates
     */
    fun downloadModel(modelId: String): Flow<DownloadProgress> = flow {
        val model = AVAILABLE_MODELS.find { it.id == modelId }
            ?: throw IllegalArgumentException("Unknown model: $modelId")
        
        val outputFile = getModelFile(modelId)
        val tempFile = File(outputFile.absolutePath + ".tmp")
        
        try {
            Log.i(TAG, "Starting download of ${model.name}")
            
            val url = URL(model.downloadUrl)
            val connection = url.openConnection()
            connection.connect()
            
            val totalBytes = connection.contentLength.toLong()
            var downloadedBytes = 0L
            
            connection.getInputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        
                        val percentage = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt()
                        } else 0
                        
                        emit(DownloadProgress(downloadedBytes, totalBytes, percentage))
                    }
                }
            }
            
            // Move temp file to final location
            if (tempFile.renameTo(outputFile)) {
                Log.i(TAG, "Successfully downloaded ${model.name}")
                emit(DownloadProgress(downloadedBytes, totalBytes, 100))
            } else {
                throw Exception("Failed to move downloaded file")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download ${model.name}", e)
            tempFile.delete()
            throw e
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Delete a downloaded model
     */
    suspend fun deleteModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        val file = getModelFile(modelId)
        val deleted = file.delete()
        if (deleted) {
            Log.i(TAG, "Deleted model: $modelId")
        }
        deleted
    }
    
    /**
     * Validate a downloaded model file
     */
    suspend fun validateModel(modelId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = getModelFile(modelId)
            if (!file.exists()) return@withContext false
            
            // Basic validation - check if file is not empty and has reasonable size
            val fileSize = file.length()
            val model = AVAILABLE_MODELS.find { it.id == modelId } ?: return@withContext false
            
            // File should be at least 80% of expected size (allowing for compression differences)
            val minExpectedSize = (model.size * 0.8).toLong()
            val maxExpectedSize = (model.size * 1.2).toLong()
            
            val isValidSize = fileSize in minExpectedSize..maxExpectedSize
            
            // TODO: Add GGUF header validation if needed
            // val hasValidHeader = validateGGUFHeader(file)
            
            Log.i(TAG, "Model validation for $modelId: size=$fileSize, expected=${model.size}, valid=$isValidSize")
            isValidSize
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating model $modelId", e)
            false
        }
    }
    
    /**
     * Get total disk space used by models
     */
    fun getTotalModelsSize(): Long {
        return modelsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
    
    /**
     * Format bytes to human readable size
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "${size.roundToInt()} ${units[unitIndex]}"
    }
}