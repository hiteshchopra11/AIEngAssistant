#include <jni.h>
#include <string>
#include <android/log.h>
#include <vector>
#include <thread>
#include <mutex>

// Include llama.cpp headers
#include "llama.h"
#include "common.h"

#define LOG_TAG "LlamaAndroid"  
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Global variables for model and context management
static std::mutex llama_mutex;
static bool backend_initialized = false;

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_loadModel(JNIEnv *env, jobject /* this */, jstring pathToModel) {
    std::lock_guard<std::mutex> lock(llama_mutex);
    
    const char *path = env->GetStringUTFChars(pathToModel, 0);
    LOGI("Loading model from: %s", path);
    
    try {
        // Set up model parameters
        llama_model_params model_params = llama_model_default_params();
        model_params.n_gpu_layers = 0; // CPU only for Android
        
        // Load the model
        llama_model *model = llama_load_model_from_file(path, model_params);
        
        if (model == nullptr) {
            LOGE("Failed to load model from: %s", path);
            env->ReleaseStringUTFChars(pathToModel, path);
            return 0;
        }
        
        LOGI("Model loaded successfully: %s", path);
        env->ReleaseStringUTFChars(pathToModel, path);
        return reinterpret_cast<jlong>(model);
        
    } catch (const std::exception& e) {
        LOGE("Exception loading model: %s", e.what());
        env->ReleaseStringUTFChars(pathToModel, path);
        return 0;
    }
}

JNIEXPORT jlong JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_newContext(JNIEnv *env, jobject /* this */, jlong modelPtr) {
    std::lock_guard<std::mutex> lock(llama_mutex);
    
    LOGI("Creating new context for model: %ld", modelPtr);
    
    if (modelPtr == 0) {
        LOGE("Invalid model pointer");
        return 0;
    }
    
    try {
        llama_model *model = reinterpret_cast<llama_model*>(modelPtr);
        
        // Set up context parameters
        llama_context_params ctx_params = llama_context_default_params();
        ctx_params.n_ctx = 2048; // Context size
        ctx_params.n_threads = std::min(4, (int)std::thread::hardware_concurrency());
        ctx_params.n_threads_batch = ctx_params.n_threads;
        
        // Create context
        llama_context *ctx = llama_new_context_with_model(model, ctx_params);
        
        if (ctx == nullptr) {
            LOGE("Failed to create context");
            return 0;
        }
        
        LOGI("Context created successfully");
        return reinterpret_cast<jlong>(ctx);
        
    } catch (const std::exception& e) {
        LOGE("Exception creating context: %s", e.what());
        return 0;
    }
}

JNIEXPORT void JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_completionInit(JNIEnv *env, jobject /* this */, jlong contextPtr, jstring prompt) {
    const char *promptStr = env->GetStringUTFChars(prompt, 0);
    LOGI("Initializing completion with prompt: %s", promptStr);
    
    // Placeholder: Will initialize completion once llama.cpp is integrated
    
    env->ReleaseStringUTFChars(prompt, promptStr);
}

JNIEXPORT jstring JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_completionLoop(JNIEnv *env, jobject /* this */, jlong contextPtr) {
    LOGI("Running completion loop for context: %ld", contextPtr);
    
    // Placeholder: Will generate tokens once llama.cpp is integrated
    // For now, return empty string
    return env->NewStringUTF("");
}

JNIEXPORT jboolean JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_backendInit(JNIEnv *env, jobject /* this */) {
    std::lock_guard<std::mutex> lock(llama_mutex);
    
    LOGI("Initializing llama backend");
    
    if (backend_initialized) {
        LOGI("Backend already initialized");
        return JNI_TRUE;
    }
    
    try {
        // Initialize the backend
        llama_backend_init();
        
        // Set logging
        llama_log_set([](ggml_log_level level, const char * text, void * user_data) {
            switch (level) {
                case GGML_LOG_LEVEL_ERROR:
                    LOGE("GGML: %s", text);
                    break;
                case GGML_LOG_LEVEL_WARN:
                    LOGD("GGML: %s", text);
                    break;
                case GGML_LOG_LEVEL_INFO:
                    LOGD("GGML: %s", text);
                    break;
                default:
                    LOGD("GGML: %s", text);
                    break;
            }
        }, nullptr);
        
        backend_initialized = true;
        LOGI("Backend initialized successfully");
        return JNI_TRUE;
        
    } catch (const std::exception& e) {
        LOGE("Exception initializing backend: %s", e.what());
        return JNI_FALSE;
    }
}

JNIEXPORT jstring JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_systemInfo(JNIEnv *env, jobject /* this */) {
    LOGI("Getting system info");
    
    try {
        std::string info = "System Information:\n";
        info += "Hardware concurrency: " + std::to_string(std::thread::hardware_concurrency()) + "\n";
        info += "Backend initialized: " + std::string(backend_initialized ? "Yes" : "No") + "\n";
        info += "Llama.cpp build info: " + std::string(llama_print_system_info()) + "\n";
        
        return env->NewStringUTF(info.c_str());
    } catch (const std::exception& e) {
        LOGE("Exception getting system info: %s", e.what());
        return env->NewStringUTF("System info unavailable");
    }
}

JNIEXPORT void JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_freeModel(JNIEnv *env, jobject /* this */, jlong modelPtr) {
    std::lock_guard<std::mutex> lock(llama_mutex);
    
    LOGI("Freeing model: %ld", modelPtr);
    
    if (modelPtr != 0) {
        try {
            llama_model *model = reinterpret_cast<llama_model*>(modelPtr);
            llama_free_model(model);
            LOGI("Model freed successfully");
        } catch (const std::exception& e) {
            LOGE("Exception freeing model: %s", e.what());
        }
    }
}

JNIEXPORT void JNICALL
Java_com_offline_english_ai_eng_assistant_LlamaAndroid_freeContext(JNIEnv *env, jobject /* this */, jlong contextPtr) {
    std::lock_guard<std::mutex> lock(llama_mutex);
    
    LOGI("Freeing context: %ld", contextPtr);
    
    if (contextPtr != 0) {
        try {
            llama_context *ctx = reinterpret_cast<llama_context*>(contextPtr);
            llama_free(ctx);
            LOGI("Context freed successfully");
        } catch (const std::exception& e) {
            LOGE("Exception freeing context: %s", e.what());
        }
    }
}

}