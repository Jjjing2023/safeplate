package com.jillliu.safeplate.eval;

import com.jillliu.safeplate.llm.EvidenceClassification;

public record EvalResult(
        EvalItem item,
        EvidenceClassification classification,
        long latencyMillis,
        String error) {

}
