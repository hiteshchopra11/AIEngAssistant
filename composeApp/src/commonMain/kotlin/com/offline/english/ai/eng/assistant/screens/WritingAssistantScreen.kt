package com.offline.english.ai.eng.assistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.offline.english.ai.eng.assistant.components.*
import com.offline.english.ai.eng.assistant.mvi.WritingAssistantViewModel
import com.offline.english.ai.eng.assistant.mvi.WritingAssistantViewAction
import com.offline.english.ai.eng.assistant.WritingMode
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WritingAssistantScreen(
    viewModel: WritingAssistantViewModel = remember { WritingAssistantViewModel() },
    onBackPressed: (() -> Unit)? = null
) {
    val state = viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AIWriterHeader(
            wordCount = state.wordCount,
            onBackPressed = onBackPressed
        )
        
        WritingModeTabs(
            selectedMode = state.selectedMode,
            onModeSelected = { mode ->
                viewModel.handleAction(WritingAssistantViewAction.SelectWritingMode(mode))
            }
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column {
                TextEditor(
                    text = state.text,
                    onTextChange = { text ->
                        viewModel.handleAction(WritingAssistantViewAction.UpdateText(text))
                    },
                    suggestions = state.suggestions,
                    modifier = Modifier.weight(1f)
                )
                
                if (state.activeSuggestions.isNotEmpty() || state.isProcessingWord || state.isProcessingSentence) {
                    SuggestionsSection(
                        suggestions = state.activeSuggestions,
                        onApplySuggestion = { suggestion ->
                            viewModel.handleAction(WritingAssistantViewAction.ApplySuggestion(suggestion))
                        },
                        onIgnoreSuggestion = { suggestion ->
                            viewModel.handleAction(WritingAssistantViewAction.IgnoreSuggestion(suggestion))
                        },
                        onApplyAll = {
                            viewModel.handleAction(WritingAssistantViewAction.ApplyAllSuggestions)
                        },
                        isProcessingWord = state.isProcessingWord,
                        isProcessingSentence = state.isProcessingSentence
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun WritingAssistantScreenPreview() {
    MaterialTheme {
        WritingAssistantScreen(
            onBackPressed = {}
        )
    }
}