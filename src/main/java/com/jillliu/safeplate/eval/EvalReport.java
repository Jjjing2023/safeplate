package com.jillliu.safeplate.eval;

import java.util.ArrayList;
import java.util.List;

import com.jillliu.safeplate.llm.ClassifyConfig;
import com.jillliu.safeplate.llm.EvidenceCategory;
import com.jillliu.safeplate.llm.EvidenceClassification;

public record EvalReport(
        ClassifyConfig config,
        String classifierName,
        int total,
        int correctCount,
        int[][] confusion,
        long totalLatencyMillis,
        int badQuoteCount,
        int errorCount,
        List<EvalResult> wrongResults,
        List<EvalResult> errorResults) {

    private static final int TEXT_PREVIEW_LENGTH = 80;

    public static EvalReport from(ClassifyConfig config, List<EvalResult> results, String classifierName) {
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

        return new EvalReport(config, classifierName, results.size(), correctCount, confusion, totalLatencyMillis,
                badQuoteCount,
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

    public String format() {

        StringBuilder sb = new StringBuilder();
        sb.append("=== Eval report ===\n");
        sb.append(String.format("classifier=%s  model=%s  prompt=%s  think=%b  temperature=%.1f\n\n",
                classifierName, config.model(), config.promptVersion(), config.think(), config.temperature()));

        sb.append(String.format("Items: %d   Errors: %d   Answered: %d   Avg latency: %.0f ms\n",
                total, errorCount, answeredCount(), averageLatencyMillis()));
        sb.append(String.format("Accuracy: %d/%d = %.1f%%\n", correctCount, answeredCount(), accuracy() * 100));
        sb.append(String.format("Bad quotes: %d (POSITIVE/NEGATIVE predictions only)\n\n", badQuoteCount));

        EvidenceCategory[] categories = EvidenceCategory.values();
        sb.append("Confusion (rows = expected, columns = predicted)\n");
        sb.append(String.format("%-12s", ""));
        for (EvidenceCategory c : categories) {
            sb.append(String.format("%13s", c));
        }
        sb.append("\n");
        for (EvidenceCategory expected : categories) {
            sb.append(String.format("%-12s", expected));
            for (EvidenceCategory predicted : categories) {
                sb.append(String.format("%13d", confusion[expected.ordinal()][predicted.ordinal()]));
            }
            sb.append("\n");
        }

        sb.append(String.format("\nWrong items (%d)\n", wrongResults.size()));
        for (EvalResult r : wrongResults) {
            EvidenceClassification c = r.classification();
            sb.append(String.format("%-5s %s -> %s\n", r.item().id(), r.item().expected(), c.category()));
            sb.append("      text:   ").append(preview(r.item().text())).append("\n");
            sb.append("      quote:  \"").append(c.quote()).append("\"\n");
            sb.append("      reason: ").append(c.reason()).append("\n");
        }

        sb.append(String.format("\nErrors (%d)\n", errorResults.size()));
        for (EvalResult r : errorResults) {
            sb.append(String.format("%-5s %s\n", r.item().id(), r.error()));
        }
        return sb.toString();

    }

    private static String preview(String text) {
        return text.length() > TEXT_PREVIEW_LENGTH ? text.substring(0, TEXT_PREVIEW_LENGTH) + "..." : text;
    }

}