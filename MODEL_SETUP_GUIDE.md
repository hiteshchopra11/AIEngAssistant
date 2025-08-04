# Model Setup Guide

## 🎉 Crash Fixed!

The app crash has been resolved. Here's what was fixed:

### ✅ Issues Resolved
1. **Updated MediaPipe version** from `0.10.14` to `0.10.23` to fix the STABLEHLO_COMPOSITE error
2. **Added proper error handling** to prevent crashes when model files are missing
3. **Fixed hardcoded model path** to support multiple model locations
4. **Made initialization graceful** - app won't crash if no model is available

## 📁 Model File Setup

To use the AI features, place your `.task` model file in one of these locations:

### Option 1: Assets Directory (Recommended)
```
composeApp/src/androidMain/assets/your-model.task
```

### Option 2: External Storage Paths
The app will also check these locations:
- `/data/local/tmp/llm/model_version.task`
- `/sdcard/Download/model.task`
- `/sdcard/models/model.task`

## 🔽 Getting Model Files

### Pre-trained Models
You can download ready-to-use `.task` files from:
- [HuggingFace MediaPipe Models](https://huggingface.co/collections/google/mediapipe-models-64f74c7a49364b7dcb5c3e6e)
- [LiteRT Community Models](https://ai.google.dev/edge/litert/models)

### Recommended Models for Mobile
- **Gemma-3 1B**: ~500MB (most efficient for mobile)
- **Gemma-2 2B**: ~1.5GB (better quality)

### Convert Your Own Model
Follow the [MediaPipe bundling guide](https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference#model_conversion) to convert PyTorch models to `.task` format.

## 🚀 How to Test

1. **Without model**: App runs normally but AI features show friendly error messages
2. **With model**: Place your `.task` file in the assets directory and the app will automatically detect and use it

## 📱 Running the App

The app will now start successfully regardless of whether a model is present:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## 📝 Logs

Check the logs to see model loading status:
- `Model initialized successfully` - Model loaded and ready
- `Model not available` - App running without AI features

## 🔧 Next Steps

1. Download a model file
2. Place it in `composeApp/src/androidMain/assets/`
3. Rebuild and run the app
4. Enjoy AI-powered features!