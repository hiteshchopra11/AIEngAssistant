package com.offline.english.ai.eng.assistant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offline.english.ai.eng.assistant.SuggestionType
import com.offline.english.ai.eng.assistant.WritingSuggestion
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun CheckIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Gray
) {
    Text(
        text = "✓",
        modifier = modifier,
        color = tint,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SuggestionsSection(
    suggestions: List<WritingSuggestion>,
    onApplySuggestion: (WritingSuggestion) -> Unit,
    onIgnoreSuggestion: (WritingSuggestion) -> Unit,
    onApplyAll: () -> Unit,
    isLoading: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F9FA),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Suggestions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                        
                        if (isLoading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFE3F2FD),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Processing...",
                                    fontSize = 10.sp,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = if (suggestions.isEmpty() && !isLoading) {
                            "No suggestions yet - start typing!"
                        } else {
                            "${suggestions.size} improvements found"
                        },
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                if (suggestions.size > 1) {
                    Button(
                        onClick = onApplyAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Apply All (${suggestions.size})",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            suggestions.forEachIndexed { index, suggestion ->
                SuggestionItem(
                    suggestion = suggestion,
                    onApply = { onApplySuggestion(suggestion) },
                    onIgnore = { onIgnoreSuggestion(suggestion) }
                )
                
                if (index < suggestions.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestion: WritingSuggestion,
    onApply: () -> Unit,
    onIgnore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = when (suggestion.type) {
                                SuggestionType.GRAMMAR -> Color(0xFFFFEBEE)
                                SuggestionType.STYLE -> Color(0xFFF3E5F5)
                                SuggestionType.CLARITY -> Color(0xFFE8F5E8)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = suggestion.type.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (suggestion.type) {
                            SuggestionType.GRAMMAR -> Color(0xFFD32F2F)
                            SuggestionType.STYLE -> Color(0xFF7B1FA2)
                            SuggestionType.CLARITY -> Color(0xFF388E3C)
                        }
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "\"${suggestion.original}\"",
                    color = Color(0xFF666666),
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "→",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Text(
                    text = "\"${suggestion.suggestion}\"",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onIgnore,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text(
                        text = "Ignore",
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CheckIcon(
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Apply",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SuggestionItemPreview() {
    MaterialTheme {
        val mockSuggestion = WritingSuggestion(
            original = "have many bugs",
            suggestion = "has many bugs",
            startIndex = 0,
            endIndex = 14,
            type = SuggestionType.GRAMMAR
        )
        
        SuggestionItem(
            suggestion = mockSuggestion,
            onApply = {},
            onIgnore = {}
        )
    }
}

@Preview
@Composable
fun SuggestionsSectionPreview() {
    MaterialTheme {
        val mockSuggestions = listOf(
            WritingSuggestion(
                original = "have many bugs",
                suggestion = "has many bugs",
                startIndex = 0,
                endIndex = 14,
                type = SuggestionType.GRAMMAR
            ),
            WritingSuggestion(
                original = "since last month",
                suggestion = "for the past month",
                startIndex = 20,
                endIndex = 36,
                type = SuggestionType.STYLE
            )
        )
        
        SuggestionsSection(
            suggestions = mockSuggestions,
            onApplySuggestion = {},
            onIgnoreSuggestion = {},
            onApplyAll = {},
            isLoading = false
        )
    }
}