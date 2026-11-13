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

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OllamaClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
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

        String body = objectMapper.writeValueAsString(root);

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
        if (contentNode.isMissingNode() || contentNode.isNull() || !contentNode.isTextual()) {
            throw new IllegalStateException("Ollama response has no text in message.content: " + response.body());
        }
        return contentNode.asText();
    }

}
