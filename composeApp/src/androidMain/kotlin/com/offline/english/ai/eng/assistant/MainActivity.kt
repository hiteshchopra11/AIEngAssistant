package com.offline.english.ai.eng.assistant

import GemmaInference
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private lateinit var gemma: GemmaInference
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        gemma = GemmaInference(this)

        // Example synchronous call
        val response = gemma.generate("Hello, who are you?")
        Log.i("MainActivity", "Response: $response")

        // Example asynchronous call
        gemma.generateAsync("Write a haiku about the ocean")
        setContent {
            AndroidApp()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AndroidApp()
}