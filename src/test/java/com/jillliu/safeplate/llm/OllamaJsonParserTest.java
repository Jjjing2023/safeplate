package com.jillliu.safeplate.llm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class OllamaJsonParserTest {

    private final OllamaJsonParser parser = new OllamaJsonParser();

    @Test
    void parsesAllFields() throws JsonProcessingException {
        EvidenceClassification res = parser.parseEvidence(
                "{\"category\": \"POSITIVE\", \"quote\": \"...\", \"reason\": \"...\" }");

        EvidenceClassification expect = new EvidenceClassification(EvidenceCategory.POSITIVE,
                "...",
                "...");
        assertEquals(expect, res);
    }

    @Test
    void rejectsUnknownCategory() {

        assertThrows(IllegalArgumentException.class, () -> parser.parseEvidence(
                "{\"category\": \"MAYBE\", \"quote\": \"...\", \"reason\": \"...\" }"));
    }

    @Test
    void acceptEmptyQuote() throws JsonProcessingException {
        EvidenceClassification res = parser.parseEvidence(
                "{\"category\": \"IRRELEVANT\", \"quote\": \"\", \"reason\": \"...\" }");

        EvidenceClassification expect = new EvidenceClassification(EvidenceCategory.IRRELEVANT, "", "...");
        assertEquals(expect, res);
    }

}
