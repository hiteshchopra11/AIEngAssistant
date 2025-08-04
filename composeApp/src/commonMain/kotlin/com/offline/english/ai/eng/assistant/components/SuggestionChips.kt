package com.offline.english.ai.eng.assistant.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestionChips(
    wordSuggestions: List<String>,
    sentenceSuggestions: List<String>,
    isLoading: Boolean,
    onWordSuggestionClick: (String) -> Unit,
    onSentenceSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Color(0xFF2E7D32)
                    )
                }
            } else {
                // Word suggestions
                if (wordSuggestions.isNotEmpty()) {
                    Text(
                        text = "Word Suggestions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        wordSuggestions.forEach { suggestion ->
                            SuggestionChip(
                                text = suggestion,
                                onClick = { onWordSuggestionClick(suggestion) },
                                backgroundColor = Color(0xFFE8F5E8)
                            )
                        }
                    }
                }
                
                // Sentence suggestions
                if (sentenceSuggestions.isNotEmpty()) {
                    Text(
                        text = "Sentence Suggestions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sentenceSuggestions.forEach { suggestion ->
                            SentenceSuggestionCard(
                                text = suggestion,
                                onClick = { onSentenceSuggestionClick(suggestion) }
                            )
                        }
                    }
                }
                
                if (wordSuggestions.isEmpty() && sentenceSuggestions.isEmpty()) {
                    Text(
                        text = "Start typing to see suggestions...",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color
) {
    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF2E7D32),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SentenceSuggestionCard(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF333333),
            modifier = Modifier.padding(12.dp)
        )
    }
}