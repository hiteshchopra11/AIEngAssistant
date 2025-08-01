# Llama.cpp Integration

This document describes the integration of llama.cpp into the AI English Assistant project.

## Overview

The project now includes a complete integration framework for llama.cpp, allowing the app to run large language models locally on Android devices. The integration is currently a **placeholder implementation** that provides the structure without the actual llama.cpp library.

## Integration Components

### 1. Native C++ Layer
- **Location**: `composeApp/src/androidMain/cpp/`
- **Files**:
  - `CMakeLists.txt` - CMake configuration for native library
  - `llama-android.cpp` - JNI wrapper with placeholder implementations

### 2. Android Kotlin Interface  
- **Location**: `composeApp/src/androidMain/kotlin/com/offline/english/ai/eng/assistant/`
- **Files**:
  - `LlamaAndroid.kt` - Main interface for llama.cpp functionality
  - `ai/AndroidLLMImplementation.kt` - Android-specific LLM implementation

### 3. Common Interface
- **Location**: `composeApp/src/commonMain/kotlin/com/offline/english/ai/eng/assistant/ai/`
- **Files**:
  - `LLMInterface.kt` - Common interface for all platforms
  - `LLMProvider.kt` - Platform-specific provider (expect/actual)

### 4. Build Configuration
- **Modified Files**:
  - `composeApp/build.gradle.kts` - Added CMake and NDK configuration
  - `composeApp/src/androidMain/AndroidManifest.xml` - Added permissions

## Current Status

### ✅ Completed
- CMake configuration structure
- JNI wrapper skeleton with logging
- Kotlin interface layer
- Build system integration
- Platform abstraction (expect/actual)
- iOS stub implementations
- Permissions configuration

### ❌ Not Yet Implemented
- Actual llama.cpp library integration
- Model loading functionality
- Text generation implementation
- Memory management for native objects

## Next Steps to Complete Integration

### 1. Add llama.cpp Source
```bash
# Option 1: Add as git submodule
git submodule add https://github.com/ggml-org/llama.cpp.git llama-cpp

# Option 2: Download and extract source
# Download llama.cpp source and place in project root
```

### 2. Update CMakeLists.txt
```cmake
# Add actual llama.cpp integration
set(LLAMA_CPP_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../llama-cpp")

# Include llama.cpp
add_subdirectory(${LLAMA_CPP_DIR} llama-build)

# Link actual libraries
target_link_libraries(${CMAKE_PROJECT_NAME}
    llama
    common
    android
    log)
```

### 3. Implement Native Methods
Replace placeholder implementations in `llama-android.cpp` with actual llama.cpp API calls:
- `llama_backend_init()`
- `llama_load_model_from_file()`
- `llama_new_context_with_model()`
- `llama_tokenize()` and `llama_decode()`

### 4. Add Model Management
- Model file validation
- Context management
- Memory optimization
- Error handling

## Usage Example (Once Implemented)

```kotlin
val llm = LLMProvider.create()

// Initialize backend
llm.initialize()

// Load model (requires .gguf model file)
llm.loadModel("/path/to/model.gguf")

// Generate text
llm.generateText("Explain AI in simple terms").collect { token ->
    print(token)
}

// Cleanup
llm.unloadModel()
```

## Model Requirements

- Models must be in GGUF format
- Recommended model sizes for mobile: 1B-7B parameters
- Models should be quantized (Q4_0, Q4_1, Q8_0) for optimal mobile performance

## Performance Considerations

- **Memory**: Large models require significant RAM
- **Storage**: Models can be 1-10GB in size
- **CPU**: ARM64 processors perform better
- **Battery**: AI inference is CPU-intensive

## Security Notes

- Models run entirely offline (no network required after download)
- User data never leaves the device
- Model files should be validated before loading
- Consider sandboxing model execution

## Dependencies

### Build Requirements
- CMake 3.22.1+
- Android NDK
- Minimum Android SDK 24 (as configured)

### Runtime Requirements
- Android 7.0+ (API 24+)
- ARM64 or ARM32 processor
- Minimum 4GB RAM recommended
- Sufficient storage for model files

## Troubleshooting

### Build Issues
1. Ensure CMake version compatibility
2. Verify NDK installation
3. Check file paths in CMakeLists.txt

### Runtime Issues
1. Verify model file format (GGUF)
2. Check available memory
3. Monitor logs for native crashes

## License Considerations

- llama.cpp: MIT License
- Ensure compliance with model licenses
- Some models have specific usage restrictions