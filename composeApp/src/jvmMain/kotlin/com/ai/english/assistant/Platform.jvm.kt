package com.ai.english.assistant

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun provideSuggestionService(): SuggestionContract? = null