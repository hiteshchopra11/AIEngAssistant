package com.ai.english.assistant

import com.ai.english.assistant.domain.WritingMode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIWriterHeader(
    wordCount: Int,
    selectedMode: WritingMode,
    onModeSelected: (WritingMode) -> Unit,
    onBackPressed: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF2E7D32),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AI",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                ModeDropdown(
                    selectedMode = selectedMode,
                    onModeSelected = onModeSelected
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Word Count
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = formatWordCount(wordCount),
                        color = Color(0xFF666666),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black
        )
    )
}

@Composable
private fun ModeDropdown(
    selectedMode: WritingMode,
    onModeSelected: (WritingMode) -> Unit
) {
    val expandedState = remember { mutableStateOf(false) }

    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        color = Color(0xFFF5F7FA),
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { expandedState.value = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = selectedMode.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("▾", color = Color(0xFF64748B), fontSize = 12.sp)
        }
    }

    DropdownMenu(
        expanded = expandedState.value,
        onDismissRequest = { expandedState.value = false }
    ) {
        WritingMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    expandedState.value = false
                    onModeSelected(mode)
                }
            )
        }
    }
}

private fun formatWordCount(count: Int): String {
    return when {
        count == 0 -> "No words"
        count == 1 -> "1 word"
        count < 1000 -> "$count words"
        count < 1000000 -> {
            val k = count / 1000.0
            "${(k * 10).toInt() / 10.0}K words"
        }
        else -> {
            val m = count / 1000000.0
            "${(m * 10).toInt() / 10.0}M words"
        }
    }
}

@Preview
@Composable
fun AIWriterHeaderPreview() {
    MaterialTheme {
        Column {
            AIWriterHeader(
                wordCount = 0,
                selectedMode = WritingMode.EMAIL,
                onModeSelected = {},
                onBackPressed = null
            )
            HorizontalDivider()
            AIWriterHeader(
                wordCount = 42,
                selectedMode = WritingMode.ESSAY,
                onModeSelected = {},
                onBackPressed = {}
            )
            HorizontalDivider()
            AIWriterHeader(
                wordCount = 1250,
                selectedMode = WritingMode.BUSINESS,
                onModeSelected = {},
                onBackPressed = {}
            )
        }
    }
}