package com.ai.english.assistant

import com.ai.english.assistant.domain.WritingMode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun WritingModeTabs(
    selectedMode: WritingMode,
    onModeSelected: (WritingMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        WritingMode.entries.forEach { mode ->
            WritingModeTab(
                mode = mode,
                isSelected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WritingModeTab(
    mode: WritingMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Color(0xFFE3F2FD) else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
            color = if (isSelected) Color(0xFF1976D2) else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview
@Composable
fun WritingModeTabsPreview() {
    MaterialTheme {
        var selectedMode by remember { mutableStateOf(WritingMode.EMAIL) }
        WritingModeTabs(
            selectedMode = selectedMode,
            onModeSelected = { selectedMode = it }
        )
    }
}