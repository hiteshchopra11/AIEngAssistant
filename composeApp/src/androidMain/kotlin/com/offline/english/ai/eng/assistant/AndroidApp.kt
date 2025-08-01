package com.offline.english.ai.eng.assistant

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.offline.english.ai.eng.assistant.screens.ModelManagementScreen

@Composable
fun AndroidApp() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        
        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(
                onNavigateToWritingAssistant = { currentScreen = AppScreen.WRITING_ASSISTANT },
                onNavigateToModelManagement = { currentScreen = AppScreen.MODEL_MANAGEMENT }
            )
            AppScreen.WRITING_ASSISTANT -> com.offline.english.ai.eng.assistant.screens.WritingAssistantScreen(
                onBackPressed = { currentScreen = AppScreen.HOME }
            )
            AppScreen.MODEL_MANAGEMENT -> ModelManagementScreen(
                onBackPressed = { currentScreen = AppScreen.HOME }
            )
        }
    }
}