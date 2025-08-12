package com.ai.english.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.ai.english.assistant.mvi.WritingAssistantViewModel
import com.ai.english.assistant.mvi.WritingAssistantIntent
import com.ai.english.assistant.domain.WritingSuggestion
import com.ai.english.assistant.domain.WritingSuggestionType
import com.ai.english.assistant.domain.GrammarSuggestionData
import com.ai.english.assistant.domain.SuggestionType

@Composable
fun WritingAssistantScreen(
    viewModel: com.ai.english.assistant.mvi.WritingAssistantViewModel = remember { 
        com.ai.english.assistant.mvi.WritingAssistantViewModel(provideSuggestionService()) 
    },
    onBackPressed: (() -> Unit)? = null
) {
    val state = viewModel.state

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        AIWriterHeader(
            wordCount = state.wordCount,
            selectedMode = state.selectedMode,
            onModeSelected = { mode ->
                viewModel.handleIntent(WritingAssistantIntent.SelectWritingMode(mode))
            },
            onBackPressed = onBackPressed
        )

        HorizontalDivider(color = Color(0xFFE5E7EB))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
                .padding(bottom = 12.dp)
        ) {
                // Editor with suggestions from state
                val editorSuggestions = remember(state.grammarSuggestions) {
                    val mappedGrammar = state.grammarSuggestions.mapNotNull { gs ->
                        val idx = state.text.indexOf(gs.original)
                        if (idx >= 0) {
                            WritingSuggestion(
                                original = gs.original,
                                suggestion = gs.suggestion,
                                startIndex = idx,
                                endIndex = idx + gs.original.length,
                                type = when (gs.type) {
                                    SuggestionType.SPELLING, SuggestionType.GRAMMAR_WORD, SuggestionType.GRAMMAR_SENTENCE -> WritingSuggestionType.GRAMMAR
                                }
                            )
                        } else null
                    }
                    mappedGrammar
                }

                TextEditor(
                    text = state.text,
                    onTextChange = { text ->
                        viewModel.handleIntent(WritingAssistantIntent.UpdateText(text))
                    },
                    suggestions = editorSuggestions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp, max = 1000.dp)
                )

                // Analyze button and inline suggestions below editor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.handleIntent(WritingAssistantIntent.AnalyzeText)
                        },
                        enabled = state.text.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFF93C5FD)
                        )
                    ) {
                        Text(
                            text = if (state.isAnalyzing) "Analyzing…" else "Analyze text",
                            color = Color.White
                        )
                    }
                    if (state.isAnalyzing) {
                        Text(text = "Working on it…", color = Color(0xFF2563EB), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                InlineSuggestionsList(
                    isLoading = state.isAnalyzing,
                    grammarSuggestions = state.grammarSuggestions,
                    onApply = { suggestion ->
                        viewModel.handleIntent(WritingAssistantIntent.ApplyGrammarSuggestion(suggestion))
                    },
                    onSkip = { suggestion -> 
                        viewModel.handleIntent(WritingAssistantIntent.RejectGrammarSuggestion(suggestion))
                    },
                    onRevert = { suggestion ->
                        // Find the applied edit and revert it
                        val appliedEdit = state.appliedEdits.find { it.appliedText == suggestion.suggestion }
                        appliedEdit?.let { edit ->
                            viewModel.handleIntent(WritingAssistantIntent.RevertEdit(edit.id))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
@Composable
private fun InlineSuggestionsList(
    isLoading: Boolean,
    grammarSuggestions: List<GrammarSuggestionData>,
    onApply: (GrammarSuggestionData) -> Unit,
    onSkip: (GrammarSuggestionData) -> Unit,
    onRevert: (GrammarSuggestionData) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Suggestions",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0F172A)
        )
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF2563EB)
                )
                Text(text = "Analyzing...", fontSize = 14.sp, color = Color(0xFF2563EB))
            }
        } else if (grammarSuggestions.isEmpty()) {
            Text(
                text = "No suggestions yet",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            grammarSuggestions.forEachIndexed { index, suggestion ->
                SuggestionRow(
                    suggestion = suggestion,
                    onApply = { onApply(suggestion) },
                    onSkip = { onSkip(suggestion) },
                    onRevert = { onRevert(suggestion) }
                )
                if (index < grammarSuggestions.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE5E7EB),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: GrammarSuggestionData,
    onApply: () -> Unit,
    onSkip: () -> Unit,
    onRevert: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Centered chip content
        Box(
            modifier = Modifier
                .background(
                    when (suggestion.type) {
                        SuggestionType.SPELLING -> Color(0xFFFEE2E2)
                        SuggestionType.GRAMMAR_WORD -> Color(0xFFDEEFFE)
                        SuggestionType.GRAMMAR_SENTENCE -> Color(0xFFD1FAE5)
                    },
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .align(Alignment.Start),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (suggestion.type) {
                    SuggestionType.SPELLING -> "Spelling"
                    SuggestionType.GRAMMAR_WORD -> "Word Choice"
                    SuggestionType.GRAMMAR_SENTENCE -> "Grammar"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = when (suggestion.type) {
                    SuggestionType.SPELLING -> Color(0xFFDC2626)
                    SuggestionType.GRAMMAR_WORD -> Color(0xFF2563EB)
                    SuggestionType.GRAMMAR_SENTENCE -> Color(0xFF059669)
                }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "\"${suggestion.original}\"",
                color = Color(0xFF6B7280),
                fontSize = 14.sp,
                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                modifier = Modifier.weight(1f)
            )
            Text(text = "→", color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = "\"${suggestion.suggestion}\"",
                color = Color(0xFF059669),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
        if (suggestion.explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = suggestion.explanation, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onApply,
                modifier = Modifier.background(Color(0xFF10B981), RoundedCornerShape(8.dp))
            ) { Text(text = "Apply", color = Color.White, fontSize = 14.sp) }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            ) { Text(text = "Skip", color = Color(0xFF6B7280), fontSize = 14.sp) }
            TextButton(
                onClick = onRevert,
                modifier = Modifier.background(Color(0xFFFDE68A), RoundedCornerShape(8.dp))
            ) { Text(text = "Revert", color = Color(0xFF92400E), fontSize = 14.sp) }
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

