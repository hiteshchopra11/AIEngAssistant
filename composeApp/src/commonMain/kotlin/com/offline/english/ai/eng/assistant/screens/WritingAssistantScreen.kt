package com.offline.english.ai.eng.assistant.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.offline.english.ai.eng.assistant.SuggestionContract
import com.offline.english.ai.eng.assistant.components.AIWriterHeader
import com.offline.english.ai.eng.assistant.components.SuggestionChips
import com.offline.english.ai.eng.assistant.components.SuggestionsSection
import com.offline.english.ai.eng.assistant.components.TextEditor
import com.offline.english.ai.eng.assistant.components.WritingModeTabs
import com.offline.english.ai.eng.assistant.mvi.WritingAssistantViewAction
import com.offline.english.ai.eng.assistant.mvi.WritingAssistantViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WritingAssistantScreen(
    viewModel: WritingAssistantViewModel = remember { WritingAssistantViewModel() },
    onBackPressed: (() -> Unit)? = null,
    suggestionService: SuggestionContract? = null
) {
    val state = viewModel.state
    
    // Collect suggestion states
    val wordSuggestions by suggestionService?.wordSuggestions?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    val sentenceSuggestions by suggestionService?.sentenceSuggestions?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    val isSuggestionLoading by suggestionService?.isLoading?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(false) }
    
    // Trigger suggestions when text changes
    LaunchedEffect(state.text) {
        if (state.text.isNotBlank() && suggestionService != null) {
            val words = state.text.split(" ")
            val currentWord = words.lastOrNull()?.takeIf { !state.text.endsWith(" ") } ?: ""
            
            // Get word suggestions for the current word being typed
            if (currentWord.isNotEmpty()) {
                suggestionService.getWordSuggestions(state.text, currentWord)
            }
            
            // Get sentence suggestions if the text ends with a sentence
            if (state.text.length > 10) {
                suggestionService.getSentenceSuggestions(state.text)
            }
        }
    }

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
                
                // Word and sentence suggestions
                if (suggestionService != null) {
                    SuggestionChips(
                        wordSuggestions = wordSuggestions.map { it.word },
                        sentenceSuggestions = sentenceSuggestions.map { it.sentence },
                        isLoading = isSuggestionLoading,
                        onWordSuggestionClick = { suggestion ->
                            val words = state.text.split(" ").toMutableList()
                            if (words.isNotEmpty() && !state.text.endsWith(" ")) {
                                words[words.size - 1] = suggestion
                            } else {
                                words.add(suggestion)
                            }
                            val newText = words.joinToString(" ")
                            viewModel.handleAction(WritingAssistantViewAction.UpdateText(newText))
                        },
                        onSentenceSuggestionClick = { suggestion ->
                            viewModel.handleAction(WritingAssistantViewAction.UpdateText(suggestion))
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
                
                // Original suggestions section for grammar/style
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
                    isLoading = state.isLoading
                )
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