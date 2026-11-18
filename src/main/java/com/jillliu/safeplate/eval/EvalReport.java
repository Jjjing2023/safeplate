package com.jillliu.safeplate.eval;

import java.util.ArrayList;
import java.util.List;

import com.jillliu.safeplate.llm.EvidenceCategory;

public record EvalReport(
        EvalConfig config,
        int total,
        int correctCount,
        int[][] confusion,
        long totalLatencyMillis,
        int badQuoteCount,
        int errorCount,
        List<EvalResult> wrongResults,
        List<EvalResult> errorResults) {

    public static EvalReport from(EvalConfig config, List<EvalResult> results) {
        int categoryCount = EvidenceCategory.values().length;
        int[][] confusion = new int[categoryCount][categoryCount];

        int errorCount = 0;
        int correctCount = 0;
        long totalLatencyMillis = 0;
        int badQuoteCount = 0;

        List<EvalResult> wrongResults = new ArrayList<>();
        List<EvalResult> errorResults = new ArrayList<>();

        for (EvalResult r : results) {
            totalLatencyMillis += r.latencyMillis();

            if (r.error() != null) {
                errorCount++;
                errorResults.add(r);
                continue;
            }

            EvalItem item = r.item();
            EvidenceCategory expected = item.expected();
            EvidenceCategory predicted = r.classification().category();
            confusion[expected.ordinal()][predicted.ordinal()]++;

            // Only POSITIVE/NEGATIVE change the level, so only their quotes must be real.
            if (predicted == EvidenceCategory.POSITIVE || predicted == EvidenceCategory.NEGATIVE) {
                String quote = r.classification().quote();
                if (quote == null || quote.isEmpty() || !item.text().contains(quote)) {
                    badQuoteCount++;
                }
            }

            if (expected != predicted) {
                wrongResults.add(r);
            } else {
                correctCount++;
            }
        }

        return new EvalReport(config, results.size(), correctCount, confusion, totalLatencyMillis, badQuoteCount,
                errorCount, wrongResults,
                errorResults);
    }

    public double averageLatencyMillis() {
        return total == 0 ? 0.0 : (double) totalLatencyMillis / total;
    }

    public int answeredCount() {
        return total - errorCount;
    }

    public double accuracy() {
        return answeredCount() == 0 ? 0.0 : (double) correctCount / answeredCount();
    }
}