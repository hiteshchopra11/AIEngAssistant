package com.offline.english.ai.eng.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offline.english.ai.eng.assistant.screens.*
import org.jetbrains.compose.ui.tooling.preview.Preview

data class WritingSuggestion(
    val original: String,
    val suggestion: String,
    val startIndex: Int,
    val endIndex: Int,
    val type: SuggestionType
)

enum class SuggestionType {
    GRAMMAR, STYLE, CLARITY
}

enum class WritingMode {
    EMAIL, ESSAY, BUSINESS, CASUAL
}

enum class AppScreen {
    HOME, WRITING_ASSISTANT, MODEL_MANAGEMENT
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
        
        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(
                onNavigateToWritingAssistant = { currentScreen = AppScreen.WRITING_ASSISTANT },
                onNavigateToModelManagement = { currentScreen = AppScreen.MODEL_MANAGEMENT }
            )
            AppScreen.WRITING_ASSISTANT -> WritingAssistantScreen(
                onBackPressed = { currentScreen = AppScreen.HOME }
            )
            AppScreen.MODEL_MANAGEMENT -> {
                // Import will be added when we can import Android-specific screens
                // For now, navigate back to home
                currentScreen = AppScreen.HOME
            }
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToWritingAssistant: () -> Unit,
    onNavigateToModelManagement: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo/Icon
        Card(
            modifier = Modifier.size(100.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AI",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Title
        Text(
            text = "AI English Assistant",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Description
        Text(
            text = "Improve your writing with AI-powered suggestions\nfor grammar, style, and clarity",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Start Button
        Button(
            onClick = onNavigateToWritingAssistant,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Start Writing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model Management Button
        OutlinedButton(
            onClick = onNavigateToModelManagement,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF2E7D32)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "📦 Manage AI Models",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Features List
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            FeatureItem("✓ Real-time grammar checking")
            FeatureItem("✓ Style improvements")
            FeatureItem("✓ Clarity suggestions")
            FeatureItem("✓ Multiple writing modes")
            FeatureItem("✓ Offline AI processing")
        }
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(onNavigateToWritingAssistant = {})
    }
}