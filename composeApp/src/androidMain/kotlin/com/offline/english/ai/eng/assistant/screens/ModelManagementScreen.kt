package com.offline.english.ai.eng.assistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offline.english.ai.eng.assistant.components.AIWriterHeader
import com.offline.english.ai.eng.assistant.models.ModelInfo
import com.offline.english.ai.eng.assistant.models.ModelManager
import com.offline.english.ai.eng.assistant.models.DownloadProgress
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ModelManagementScreen(
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val modelManager = remember { ModelManager(context) }
    val scope = rememberCoroutineScope()
    
    var availableModels by remember { mutableStateOf(modelManager.getAvailableModels()) }
    var downloadedModels by remember { mutableStateOf(modelManager.getDownloadedModels()) }
    var downloadProgress by remember { mutableStateOf<Map<String, DownloadProgress>>(emptyMap()) }
    var isDownloading by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Refresh downloaded models on screen load
    LaunchedEffect(Unit) {
        downloadedModels = modelManager.getDownloadedModels()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AIWriterHeader(
            wordCount = 0,
            onBackPressed = onBackPressed
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "AI Models",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Download and manage AI models for offline use",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Storage info
            val totalSize = modelManager.getTotalModelsSize()
            if (totalSize > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💾",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "Storage Used",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            Text(
                                text = modelManager.formatFileSize(totalSize),
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableModels) { model ->
                    val isDownloaded = downloadedModels.any { it.first.id == model.id }
                    val currentDownload = downloadProgress[model.id]
                    val isCurrentlyDownloading = isDownloading.contains(model.id)
                    
                    ModelItemCard(
                        model = model,
                        isDownloaded = isDownloaded,
                        downloadProgress = currentDownload,
                        isDownloading = isCurrentlyDownloading,
                        onDownload = {
                            scope.launch {
                                isDownloading = isDownloading + model.id
                                try {
                                    modelManager.downloadModel(model.id).collect { progress ->
                                        downloadProgress = downloadProgress + (model.id to progress)
                                    }
                                    // Refresh downloaded models after successful download
                                    downloadedModels = modelManager.getDownloadedModels()
                                } catch (e: Exception) {
                                    // Handle download error
                                } finally {
                                    isDownloading = isDownloading - model.id
                                    downloadProgress = downloadProgress - model.id
                                }
                            }
                        },
                        onDelete = {
                            scope.launch {
                                if (modelManager.deleteModel(model.id)) {
                                    downloadedModels = modelManager.getDownloadedModels()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelItemCard(
    model: ModelInfo,
    isDownloaded: Boolean,
    downloadProgress: DownloadProgress?,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        if (model.isRecommended) {
                            Text(
                                text = "⭐",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = model.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Text(
                        text = "Size: ${ModelManager(LocalContext.current).formatFileSize(model.size)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Download progress bar
            if (isDownloading && downloadProgress != null) {
                Column {
                    LinearProgressIndicator(
                        progress = { downloadProgress.percentage / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2E7D32),
                    )
                    Text(
                        text = "${downloadProgress.percentage}% - ${ModelManager(LocalContext.current).formatFileSize(downloadProgress.bytesDownloaded)} / ${ModelManager(LocalContext.current).formatFileSize(downloadProgress.totalBytes)}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDownloaded) {
                        OutlinedButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Text("Delete", fontSize = 14.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = { /* TODO: Load model */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Text("Use Model", fontSize = 14.sp, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            enabled = !isDownloading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text(
                                text = if (isDownloading) "Downloading..." else "Download",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ModelManagementScreenPreview() {
    MaterialTheme {
        ModelManagementScreen(onBackPressed = {})
    }
}