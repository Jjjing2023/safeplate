package com.jillliu.safeplate.eval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jillliu.safeplate.llm.EvidenceCategory;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvalItem(
        String id,
        EvidenceCategory expected,
        String text) {

}
