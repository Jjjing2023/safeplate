package com.jillliu.safeplate.llm;

public enum EvidenceCategory {
    POSITIVE, NEGATIVE, BOILERPLATE, IRRELEVANT;

    public static EvidenceCategory fromOllama(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("EvidenceCategory not specified: " + value);
        }
        return EvidenceCategory.valueOf(value);
    }
}
