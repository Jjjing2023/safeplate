package com.jillliu.safeplate.assess;

import com.jillliu.safeplate.llm.EvidenceClassification;

public record ClassifiedEvidence(EvidenceClassification classification, EvidenceSource source) {

}
