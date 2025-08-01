package com.offline.english.ai.eng.assistant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform