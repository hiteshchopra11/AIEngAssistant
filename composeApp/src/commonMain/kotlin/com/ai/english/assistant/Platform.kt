package com.ai.english.assistant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform