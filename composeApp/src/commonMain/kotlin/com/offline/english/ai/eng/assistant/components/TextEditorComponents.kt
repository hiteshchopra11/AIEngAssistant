package com.offline.english.ai.eng.assistant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.offline.english.ai.eng.assistant.SuggestionType
import com.offline.english.ai.eng.assistant.WritingSuggestion
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                text = "Start typing to get real-time suggestions...",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            if (text.isNotEmpty() && suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = buildAnnotatedString {
                                var lastIndex = 0
                                
                                suggestions.forEach { suggestion ->
                                    if (suggestion.startIndex >= lastIndex && suggestion.endIndex <= text.length) {
                                        append(text.substring(lastIndex, suggestion.startIndex))
                                        
                                        withStyle(
                                            style = SpanStyle(
                                                background = when (suggestion.type) {
                                                    SuggestionType.GRAMMAR -> Color(0xFFFFEBEE)
                                                    SuggestionType.STYLE -> Color(0xFFF3E5F5)
                                                    SuggestionType.CLARITY -> Color(0xFFE8F5E8)
                                                },
                                                color = when (suggestion.type) {
                                                    SuggestionType.GRAMMAR -> Color(0xFFD32F2F)
                                                    SuggestionType.STYLE -> Color(0xFF7B1FA2)
                                                    SuggestionType.CLARITY -> Color(0xFF388E3C)
                                                }
                                            )
                                        ) {
                                            append(text.substring(suggestion.startIndex, suggestion.endIndex))
                                        }
                                        
                                        lastIndex = suggestion.endIndex
                                    }
                                }
                                
                                if (lastIndex < text.length) {
                                    append(text.substring(lastIndex))
                                }
                            },
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
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
                type = SuggestionType.STYLE
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