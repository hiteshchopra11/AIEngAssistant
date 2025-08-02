import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference

class GemmaInference(private val context: Context) {

    private lateinit var llmInference: LlmInference

    fun initialize() {
        val taskOptions = LlmInference.LlmInferenceOptions.builder()
            .setModelPath("/data/local/tmp/llm/model_version.task")
            .setTopK(64)
            .build()

        llmInference = LlmInference.createFromOptions(context, taskOptions)
    }

    /** Run inference synchronously */
    fun generate(inputPrompt: String): String {
        if (!::llmInference.isInitialized) initialize()
        return llmInference.generateResponse(inputPrompt)
    }

    /** Run inference asynchronously */
    fun generateAsync(inputPrompt: String) {
        if (!::llmInference.isInitialized) initialize()

        llmInference.generateResponseAsync(inputPrompt)
    }
}
