package com.jillliu.safeplate.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OllamaJsonParser {
    private final ObjectMapper objectMapper;

    public OllamaJsonParser() {
        this.objectMapper = new ObjectMapper();
    }

    public EvidenceClassification parseEvidence(String content) throws JsonMappingException, JsonProcessingException {
        JsonNode node = objectMapper.readTree(content);
        EvidenceCategory category = EvidenceCategory.fromOllama(node.path("category").asText());
        String quote = node.path("quote").asText();
        String reason = node.path("reason").asText();
        return new EvidenceClassification(category, quote, reason);
    }

}
