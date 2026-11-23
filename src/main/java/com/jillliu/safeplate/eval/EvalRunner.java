package com.jillliu.safeplate.eval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jillliu.safeplate.llm.EvidenceClassification;
import com.jillliu.safeplate.llm.EvidenceClassifier;

public class EvalRunner {
    private final EvidenceClassifier classifier;

    public EvalRunner(EvidenceClassifier classifier) {
        this.classifier = classifier;
    }

    public List<EvalResult> run(List<EvalItem> items) throws InterruptedException {
        List<EvalResult> results = new ArrayList<>();
        for (EvalItem item : items) {
            long start = System.currentTimeMillis();
            try {
                EvidenceClassification c = classifier.classify(item.text());
                long latency = System.currentTimeMillis() - start;
                EvalResult result = new EvalResult(item, c, latency, null);
                results.add(result);
            } catch (IOException | RuntimeException e) {
                long latency = System.currentTimeMillis() - start;
                EvalResult result = new EvalResult(item, null, latency, e.toString());
                results.add(result);
            }
        }
        return results;

    }

}
