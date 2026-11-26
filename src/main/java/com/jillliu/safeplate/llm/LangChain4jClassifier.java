package com.jillliu.safeplate.llm;

import java.time.Duration;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

/**
 * Classifies evidence with langchain4j AI Services on a local Ollama model.
 * 
 * <p>
 * Sends the same requests as {@link OllamaClient} (same prompt, user message,
 * think, temperature and JSON schema), so the two can be compared with the same
 * eval.
 */
public class LangChain4jClassifier implements EvidenceClassifier {
    private final ClassifyService service;

    interface ClassifyService {
        EvidenceClassification classify(String userMessage);
    }

    public LangChain4jClassifier(ClassifyConfig config) {
        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(config.model())
                .think(config.think())
                .temperature(config.temperature())
                .timeout(Duration.ofSeconds(120))
                // Send the schema in Ollama's "format" field. Without this,
                // AI Services appends format instructions to the user message.
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .build();

        String systemPrompt = ClassifyPrompts.promptFor(config.promptVersion());

        this.service = AiServices.builder(ClassifyService.class)
                .chatModel(model)
                .systemMessage(systemPrompt)
                .build();

    }

    @Override
    public EvidenceClassification classify(String text) {
        // Same prefix as OllamaClient, so eval results stay comparable.
        return service.classify("Evidence: " + text);
    }

}
