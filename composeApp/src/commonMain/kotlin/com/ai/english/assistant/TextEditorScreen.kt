package com.ai.english.assistant

import kotlin.compareTo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.ai.english.assistant.domain.WritingSuggestion
import com.ai.english.assistant.domain.WritingSuggestionType

@Composable
fun TextEditor(
    text: String,
    onTextChange: (String) -> Unit,
    suggestions: List<WritingSuggestion>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Display text with highlighting overlay
                if (text.isNotEmpty() && suggestions.isNotEmpty()) {
                    SelectionContainer {
                        Text(
                            text = buildAnnotatedString {
                                var lastIndex = 0
                                
                                // Sort suggestions by start index to apply them in order
                                val sortedSuggestions = suggestions.filter { 
                                    it.startIndex >= 0 && it.endIndex <= text.length && it.startIndex < it.endIndex
                                }.sortedBy { it.startIndex }
                                
                                sortedSuggestions.forEach { suggestion ->
                                    if (suggestion.startIndex >= lastIndex) {
                                        // Add normal text before the suggestion
                                        append(text.substring(lastIndex, suggestion.startIndex))
                                        
                                        // Add highlighted text for the suggestion
                                        withStyle(
                                            style = SpanStyle(
                                                background = when (suggestion.type) {
                                                    WritingSuggestionType.GRAMMAR -> Color(0xFFFFE7E7)
                                                    WritingSuggestionType.STYLE -> Color(0xFFF3E5F5)
                                                    WritingSuggestionType.CLARITY -> Color(0xFFE8F5E8)
                                                },
                                                color = when (suggestion.type) {
                                                    WritingSuggestionType.GRAMMAR -> Color(0xFFD32F2F)
                                                    WritingSuggestionType.STYLE -> Color(0xFF7B1FA2)
                                                    WritingSuggestionType.CLARITY -> Color(0xFF388E3C)
                                                }
                                            )
                                        ) {
                                            append(text.substring(suggestion.startIndex, suggestion.endIndex))
                                        }
                                        
                                        lastIndex = suggestion.endIndex
                                    }
                                }
                                
                                // Add any remaining text
                                if (lastIndex < text.length) {
                                    append(text.substring(lastIndex))
                                }
                            },
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Basic text field for editing (auto-resizes with content)
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = if (suggestions.isNotEmpty()) Color.Transparent else Color.Black
                    ),
                    decorationBox = { innerTextField ->
                        if (text.isEmpty()) {
                            Text(
                                text = "Type your text… then tap Analyze",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

        }
    }
}

@Preview
@Composable
fun TextEditorPreview() {
    MaterialTheme {
        var text by remember {
            mutableStateOf("I have been working on this project since last month and I think it will be ready soon.")
        }

        val mockSuggestions = listOf(
            WritingSuggestion(
                original = "since last month",
                suggestion = "for the past month",
                startIndex = 45,
                endIndex = 61,
                type = WritingSuggestionType.STYLE
            )
        )

        TextEditor(
            text = text,
            onTextChange = { text = it },
            suggestions = mockSuggestions,
            modifier = Modifier.height(300.dp)
        )
    }
}