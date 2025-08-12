package com.ai.english.assistant

actual fun provideSuggestionService(): SuggestionContract? = SuggestionsService()
