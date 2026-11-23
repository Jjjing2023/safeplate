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

public class OllamaClient implements EvidenceClassifier {
    private static final String API_ENDPOINT = "http://localhost:11434/api/chat";
    private final String systemPrompt;
    private final ClassifyConfig config;

    private static final String CLASSIFY_SYSTEM_RULE_V1 = """
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

    private static final String CLASSIFY_SYSTEM_RULE_V2 = """
            You classify ONE piece of restaurant evidence (a customer review or text from the \
            restaurant's website) about how the restaurant handles dietary restrictions and food allergies. \
            Any restriction or allergy counts, not only gluten-free or dairy-free. \
            Read the WHOLE text before deciding. Answer in JSON.

            category:
            POSITIVE = the restaurant accommodated, or states it is willing to accommodate, a dietary \
            restriction or allergy. For example: staff checked ingredients or changed a dish; a diner told \
            the staff about an allergy and had a good meal; a diner with celiac disease or an allergy ate \
            without problems; the menu marks gluten-free or dairy-free items; the restaurant offers an \
            allergen menu or accommodates on request or with advance notice. A cross-contamination \
            disclaimer does not cancel a POSITIVE.
            NEGATIVE = the restaurant refused or says it cannot accommodate (no substitutions, an ingredient \
            cannot be removed, dishes are very difficult to modify), staff were dismissive, or a diner \
            reported a reaction or got sick. A reported reaction is NEGATIVE even if the restaurant also \
            tried to accommodate.
            BOILERPLATE = generic text that neither offers nor refuses anything: asking guests to mention \
            allergies, saying no item can be guaranteed allergen-free or that not every request can be met, \
            cross-contamination warnings, and food-safety warnings about raw or undercooked food.
            IRRELEVANT = nothing about the restaurant handling a restriction. Vegan or vegetarian options \
            alone are IRRELEVANT, because vegan does not mean gluten-free or dairy-free. A reviewer's own \
            food preferences are IRRELEVANT.

            quote = copy the exact words from the evidence that support the category, or empty string for IRRELEVANT.
            reason = one short sentence.
            """;
    private static final String CLASSIFY_SYSTEM_RULE_V3 = """
            You classify ONE piece of restaurant evidence (a customer review or text from the \
            restaurant's website) about dietary restrictions and food allergies at the restaurant. \
            Any restriction or allergy counts, not only gluten-free or dairy-free. \
            Read the WHOLE text before deciding. Answer in JSON.

            category:
            POSITIVE = the restaurant accommodated, or states it is willing to accommodate, a dietary \
            restriction or allergy, or the evidence says the restaurant has gluten-free or dairy-free \
            options or dishes. For example: staff checked ingredients or changed a dish; a diner told \
            the staff about an allergy and had a good meal; a diner with celiac disease or an allergy ate \
            without problems; a review mentions gluten-free dishes; the menu marks gluten-free or \
            dairy-free items; the restaurant offers an allergen menu or accommodates on request or with \
            advance notice. A cross-contamination disclaimer does not cancel a POSITIVE.
            NEGATIVE = the restaurant refused or says it cannot accommodate (no substitutions, dishes \
            are very difficult to modify), staff were dismissive, or a diner reported a reaction or got \
            sick. A reported reaction is NEGATIVE even if the restaurant also tried to accommodate.
            BOILERPLATE = generic text that neither offers nor refuses anything: asking guests to mention \
            allergies, saying no item can be guaranteed allergen-free, that some ingredients cannot be \
            completely removed, or that not every request can be met, cross-contamination warnings, and \
            food-safety warnings about raw or undercooked food.
            IRRELEVANT = nothing about dietary restrictions or allergies at the restaurant. Vegan or \
            vegetarian options alone are IRRELEVANT, because vegan does not mean gluten-free or \
            dairy-free. A reviewer's own food preferences are IRRELEVANT.

            quote = copy the exact words from the evidence that support the category, without shortening \
            them or using "...", or empty string for IRRELEVANT.
            reason = one short sentence.
            """;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final OllamaJsonParser parser;

    public OllamaClient(ClassifyConfig config) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.parser = new OllamaJsonParser();
        this.config = config;
        this.systemPrompt = promptFor(config.promptVersion());
    }

    public static String promptFor(String version) {
        return switch (version) {
            case "v1" -> CLASSIFY_SYSTEM_RULE_V1;
            case "v2" -> CLASSIFY_SYSTEM_RULE_V2;
            case "v3" -> CLASSIFY_SYSTEM_RULE_V3;
            default -> throw new IllegalArgumentException("Unknown prompt version: " + version);
        };
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
        root.put("model", config.model());
        ArrayNode messages = root.putArray("messages");
        ObjectNode message = messages.addObject();
        message.put("role", "user");
        message.put("content", userMessage);

        root.put("stream", false);
        root.put("think", false);

        return this.send(root);
    }

    @Override
    public EvidenceClassification classify(String text) throws IOException, InterruptedException {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", config.model());
        root.put("stream", false);
        root.put("think", config.think());
        ObjectNode optionNode = root.putObject("options");
        optionNode.put("temperature", config.temperature());
        ArrayNode messages = root.putArray("messages");
        ObjectNode systemNode = messages.addObject();
        systemNode.put("role", "system");
        systemNode.put("content", systemPrompt);
        ObjectNode userNode = messages.addObject();
        userNode.put("role", "user");
        userNode.put("content", "Evidence: " + text);
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
