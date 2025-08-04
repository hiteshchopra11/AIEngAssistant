package com.offline.english.ai.eng.assistant

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.offline.english.ai.eng.assistant.screens.WritingAssistantScreen

@Composable
fun AndroidApp(suggestionService: SuggestionService? = null) {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(
                onNavigateToWritingAssistant = { currentScreen = AppScreen.WRITING_ASSISTANT }
            )

            AppScreen.WRITING_ASSISTANT -> WritingAssistantScreen(
                onBackPressed = { currentScreen = AppScreen.HOME },
                suggestionService = suggestionService
            )
        }
    }
}