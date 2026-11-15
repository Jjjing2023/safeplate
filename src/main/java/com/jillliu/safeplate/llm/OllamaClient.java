package com.jillliu.safeplate.llm;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class OllamaClient {
    private static final String MODEL_NAME = "qwen3:8b";
    private static final String API_ENDPOINT = "http://localhost:11434/api/chat";
    private static final String CLASSIFY_SYSTEM_RULE = """
                You classify ONE piece of restaurant evidence about dietary accommodation \
                (gluten-free, dairy-free). Answer in JSON.
                category:
                POSITIVE = a specific experience or explicit statement that the restaurant accommodated a dietary restriction;
                NEGATIVE = refusal, 'no substitutions', or a reported reaction;
                BOILERPLATE = generic allergy disclaimers like 'please inform your server of any allergies';
                IRRELEVANT = nothing about dietary needs.
                quote = the exact words from the evidence that support the category, or empty string.
                reason = one short sentence.
            """;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OllamaJsonParser parser;

    public OllamaClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.parser = new OllamaJsonParser();
    }

    private String send(ObjectNode requestRoot) throws IOException, InterruptedException {
        String body = objectMapper.writeValueAsString(requestRoot);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_ENDPOINT))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Ollama chat failed: HTTP " + response.statusCode() + " " + response.body());
        }
        JsonNode responseRoot = objectMapper.readTree(response.body());
        JsonNode contentNode = responseRoot.path("message").path("content");
        if (!contentNode.isTextual()) {
            throw new IllegalStateException("Ollama response has no text in message.content: " + response.body());
        }
        return contentNode.asText();
    }

    public String chat(String userMessage) throws IOException, InterruptedException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", MODEL_NAME);
        ArrayNode messages = root.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", userMessage);

        root.put("stream", false);
        root.put("think", false);

        return this.send(root);
    }

    public EvidenceClassification classify(String evidence) throws IOException, InterruptedException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", MODEL_NAME);
        root.put("stream", false);
        root.put("think", false);
        ObjectNode optionNode = root.putObject("options");
        optionNode.put("temperature", 0);
        ArrayNode messages = root.putArray("messages");
        ObjectNode systemNode = messages.addObject();
        systemNode.put("role", "system");
        systemNode.put("content", CLASSIFY_SYSTEM_RULE);
        ObjectNode userNode = messages.addObject();
        userNode.put("role", "user");
        userNode.put("content", "Evidence: " + evidence);
        ObjectNode format = root.putObject("format");
        format.put("type", "object");

        ObjectNode properties = format.putObject("properties");
        ObjectNode category = properties.putObject("category");
        category.put("type", "string");

        ArrayNode enumArray = category.putArray("enum");
        enumArray.add("POSITIVE");
        enumArray.add("NEGATIVE");
        enumArray.add("BOILERPLATE");
        enumArray.add("IRRELEVANT");

        ObjectNode quote = properties.putObject("quote");
        ObjectNode reason = properties.putObject("reason");
        quote.put("type", "string");
        reason.put("type", "string");
        ArrayNode requiredArray = format.putArray("required");
        requiredArray.add("category");
        requiredArray.add("quote");
        requiredArray.add("reason");

        String content = this.send(root);
        return parser.parseEvidence(content);
    }
}
