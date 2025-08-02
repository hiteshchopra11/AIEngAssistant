package com.offline.english.ai.eng.assistant

import android.app.Application
import android.content.Context

class AIEngAssistantApplication : Application() {
    
    companion object {
        @Volatile
        private var instance: AIEngAssistantApplication? = null
        
        fun getInstance(): AIEngAssistantApplication {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
        
        fun getContext(): Context {
            return getInstance().applicationContext
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}