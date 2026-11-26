package com.jillliu.safeplate.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One LLM classification of a piece of evidence.
 * 
 * <p>
 * All fields are marked required so that langchain4j puts them in the
 * schema's "required" list (its default is optional). This matches the schema
 * in {@link OllamaClient}, and the eval relies on category never being null.
 * 
 * @param category which of the four categories the evidence falls into
 * @param quote    exact words from the evidence that support the category;
 *                 {@code ""} for IRRELEVANT
 * @param reason   one short sentence explaining the category
 */
public record EvidenceClassification(
        @JsonProperty(required = true) EvidenceCategory category,
        @JsonProperty(required = true) String quote,
        @JsonProperty(required = true) String reason) {
}
