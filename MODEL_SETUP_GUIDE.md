# 📦 GGUF Model Setup Guide

This guide shows you multiple ways to add GGUF model files to your AI English Assistant app.

## 🚀 Method 1: Download from Internet (Recommended)

### ✅ What's Implemented
The app now includes a complete model management system with UI:

- **Model Management Screen**: Navigate from home screen → "📦 Manage AI Models"
- **Automatic Downloads**: Pre-configured popular models ready to download
- **Progress Tracking**: Real-time download progress with pause/resume
- **Storage Management**: View disk usage and delete models
- **Model Validation**: Automatic file integrity checking

### 📱 How to Use
1. **Open the app** and tap "📦 Manage AI Models"
2. **Choose a model** from the available list:
   - **TinyLlama 1.1B** (500MB) - Recommended for testing
   - **Microsoft Phi-2** (1.5GB) - Good balance of size/quality
   - **Llama 2 7B Chat** (4GB) - High quality, requires powerful device
3. **Tap "Download"** and wait for completion
4. **Tap "Use Model"** once downloaded

### 🔧 Pre-configured Models
```kotlin
// Available in ModelManager.kt
val AVAILABLE_MODELS = listOf(
    ModelInfo(
        id = "tinyllama-1.1b-chat-q4_0",
        name = "TinyLlama 1.1B Chat",
        downloadUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_0.gguf",
        size = 500_000_000L, // ~500MB
        isRecommended = true
    ),
    // ... more models
)
```

---

## 📁 Method 2: Bundle with App Assets

### For Small Models (<50MB)
You can bundle very small models directly with the app:

#### Step 1: Add Model to Assets
```bash
# Create assets directory
mkdir -p composeApp/src/androidMain/assets/models

# Download a tiny model (example)
curl -L "https://huggingface.co/microsoft/DialoGPT-small/resolve/main/model.gguf" \
  -o composeApp/src/androidMain/assets/models/tiny-model.gguf
```

#### Step 2: Create Asset Loader
```kotlin
// File: AssetModelLoader.kt
class AssetModelLoader(private val context: Context) {
    fun copyAssetToStorage(assetPath: String, targetFile: File): Boolean {
        return try {
            context.assets.open(assetPath).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

#### Step 3: Load from Assets
```kotlin
val assetLoader = AssetModelLoader(context)
val modelFile = File(context.filesDir, "models/tiny-model.gguf")
assetLoader.copyAssetToStorage("models/tiny-model.gguf", modelFile)
```

---

## 💾 Method 3: Manual File Placement

### Using ADB (Development)
```bash
# Push model file to device
adb push path/to/your/model.gguf /sdcard/Download/

# Or to app's private directory
adb push model.gguf /data/data/com.offline.english.ai.eng.assistant/files/models/
```

### Using File Manager (User)
1. Download GGUF model to device
2. Place in `/sdcard/Download/` or app's documents folder
3. Use file picker in app to select model

---

## 🌐 Method 4: Custom Model URLs

### Add Your Own Models
Update `ModelManager.kt` to include custom models:

```kotlin
val CUSTOM_MODELS = listOf(
    ModelInfo(
        id = "my-custom-model",
        name = "My Custom Model",
        description = "Custom trained model for specific tasks",
        size = 2_000_000_000L,
        downloadUrl = "https://your-server.com/path/to/model.gguf",
        isRecommended = false
    )
)
```

---

## 📋 Recommended Models for Mobile

### Tiny Models (Good for Testing)
- **TinyLlama 1.1B Chat Q4_0** - 500MB
- **DistilBERT Q8_0** - 250MB
- **MobileBERT Q4_0** - 100MB

### Small Models (Good Performance)
- **Microsoft Phi-2 Q4_0** - 1.5GB
- **Qwen-1.8B Chat Q4_0** - 1.2GB
- **StableLM-2-1.6B Q4_0** - 1GB

### Medium Models (High Quality)
- **Llama-2-7B Chat Q4_0** - 4GB
- **Mistral-7B Instruct Q4_0** - 4GB
- **CodeLlama-7B Q4_0** - 4GB (for code tasks)

---

## 🔍 Model Sources

### Hugging Face Hub
- **TheBloke**: Best quantized models - `https://huggingface.co/TheBloke`
- **Microsoft**: Official models - `https://huggingface.co/microsoft`
- **Meta**: Llama models - `https://huggingface.co/meta-llama`

### Direct GGUF Downloads
```bash
# TinyLlama (Recommended for first test)
curl -L "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_0.gguf" -o tinyllama.gguf

# Phi-2 (Good balance)
curl -L "https://huggingface.co/microsoft/phi-2-GGUF/resolve/main/phi-2.q4_0.gguf" -o phi2.gguf

# Llama-2-7B (High quality)
curl -L "https://huggingface.co/TheBloke/Llama-2-7B-Chat-GGUF/resolve/main/llama-2-7b-chat.q4_0.gguf" -o llama2-7b.gguf
```

---

## ⚡ Quick Test Setup

### 1. Test with TinyLlama (Easiest)
```bash
# Download directly to your computer
curl -L "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.q4_0.gguf" -o tinyllama.gguf

# Push to Android device
adb push tinyllama.gguf /sdcard/Download/

# Or use the app's download feature (recommended)
```

### 2. Load in App
```kotlin
val llm = LLMProvider.create()
llm.initialize()
llm.loadModel("/sdcard/Download/tinyllama.gguf")
llm.generateText("Hello, how are you?").collect { token ->
    print(token)
}
```

---

## 🛠️ Troubleshooting

### Model Won't Load
- ✅ Check file size (should be > 100MB for most models)
- ✅ Verify GGUF format (not GGML or other formats)
- ✅ Ensure sufficient RAM (model size + 2GB free)
- ✅ Check file permissions

### Download Fails
- ✅ Check internet connection
- ✅ Verify storage space available
- ✅ Try alternative download URLs
- ✅ Check if Hugging Face is accessible

### Performance Issues
- ✅ Use quantized models (Q4_0, Q4_1, Q8_0)
- ✅ Close other apps to free RAM
- ✅ Use ARM64 device if possible
- ✅ Try smaller model if device is limited

---

## 📊 Storage Requirements

| Model Size | Device RAM | Storage | Inference Speed |
|------------|------------|---------|-----------------|
| 500MB      | 2GB+       | 1GB     | Fast            |
| 1.5GB      | 4GB+       | 3GB     | Good            |
| 4GB        | 6GB+       | 8GB     | Slower          |
| 7GB+       | 8GB+       | 15GB    | Very Slow       |

---

## 🎉 Success Indicators

### App is Working When:
- ✅ Model downloads complete successfully
- ✅ "Use Model" button appears after download
- ✅ Text generation produces actual responses (not placeholder text)
- ✅ Responses are relevant to prompts
- ✅ No crashes during model loading

The easiest way to start is with **Method 1** using the built-in download feature. Just tap "📦 Manage AI Models" and download TinyLlama for your first test!