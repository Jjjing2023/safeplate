package com.jillliu.safeplate.llm;

import java.io.IOException;

public interface EvidenceClassifier {

    EvidenceClassification classify(String text) throws IOException, InterruptedException;

}
