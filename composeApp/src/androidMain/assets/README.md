# Model Files Directory

Place your `.task` model files in this directory.

## Supported Model Files
- `.task` files created with MediaPipe bundler
- Models should be optimized for mobile devices

## Where to get models:
1. Download from Hugging Face using MediaPipe bundler
2. Use Google's pre-trained Gemma models
3. Convert your own models using the MediaPipe conversion tools

## Example model placement:
- `gemma-2b.task`
- `model.task`
- `your-custom-model.task`

The app will automatically detect and load the first `.task` file found in this directory.

## Alternative model locations:
If you prefer to place models elsewhere, the app also checks:
- `/data/local/tmp/llm/model_version.task`
- `/sdcard/Download/model.task`
- `/sdcard/models/model.task`