package com.offline.english.ai.eng.assistant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var gemma: GemmaInference
    private lateinit var suggestionService: SuggestionService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Initialize GemmaInference but don't crash if model is not available
        gemma = GemmaInference(this)
        suggestionService = SuggestionService(this)
        
        // Try to initialize the model (this will gracefully fail if no model is found)
        if (gemma.initialize()) {
            Log.i("MainActivity", "Model initialized successfully")
            
            // Run inference in a coroutine to avoid blocking the main thread
            lifecycleScope.launch {
                try {
                    // Try different prompt formats for Gemma
                    val prompts = listOf(
                        "<start_of_turn>user\nHello, who are you?<end_of_turn>\n<start_of_turn>model\n",
                        "User: Hello, who are you?\nAssistant:",
                        "Hello, who are you?"
                    )
                    
                    for ((index, prompt) in prompts.withIndex()) {
                        Log.i("MainActivity", "Testing prompt format ${index + 1}: $prompt")
                        val response = gemma.generate(prompt)
                        Log.i("MainActivity", "Response ${index + 1}: $response")
                        
                        // If we get a good response (not just repeated characters), break
                        if (response.length > 10 && !response.matches(Regex("^(.)\\1+$"))) {
                            Log.i("MainActivity", "Found working prompt format!")
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error in generation: ${e.message}", e)
                }
            }
            
            // Example asynchronous call
            gemma.generateAsync("Write a haiku about the ocean")
        } else {
            Log.w("MainActivity", "Model not available. App will run without AI features.")
        }
        
        setContent {
            AndroidApp(suggestionService)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::gemma.isInitialized) {
            gemma.cleanup()
        }
        if (::suggestionService.isInitialized) {
            suggestionService.cleanup()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AndroidApp()
}