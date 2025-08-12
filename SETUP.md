# Development Setup Guide

## Prerequisites
- Android Studio or IntelliJ IDEA
- JDK 17 or higher
- For iOS development: Xcode (macOS only)

## Firebase Configuration (Required for Android AI Features)

### 1. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Follow the setup wizard

### 2. Configure Firebase AI
1. In your Firebase project console, navigate to "Build" → "Firebase AI"
2. Enable the service and follow setup instructions

### 3. Add Android App
1. Click "Add app" → Android
2. Package name: `com.ai.english.assistant`
3. Download the `google-services.json` file

### 4. Add Configuration Files
```bash
# Copy the downloaded file to both debug and release directories
cp ~/Downloads/google-services.json composeApp/src/debug/
cp ~/Downloads/google-services.json composeApp/src/release/
```

### 5. Verify Setup
Run the Android debug build:
```bash
./gradlew :composeApp:assembleDebug
```

## Security Notes
- **Never commit Firebase config files** - they contain sensitive API keys
- The `.gitignore` file already excludes these files
- Template files are provided for reference only

## Build Commands
```bash
# Build all platforms
./gradlew build

# Android only
./gradlew :composeApp:assembleDebug

# iOS (requires Xcode)
open iosApp/iosApp.xcodeproj

# Desktop
./gradlew :composeApp:run
```

## Troubleshooting

### "google-services.json not found"
- Ensure you've copied the Firebase configuration files to both debug and release directories
- Check that file names are exactly `google-services.json` (case-sensitive)

### Build errors
- Clean build: `./gradlew clean build`
- Check JDK version: `java -version` (should be 17+)

### Firebase AI not working
- Verify Firebase AI is enabled in your Firebase console
- Check Android logs for API key errors
- Ensure your Firebase project has sufficient quota