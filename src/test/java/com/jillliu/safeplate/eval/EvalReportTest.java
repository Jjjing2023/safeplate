package com.jillliu.safeplate.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.jillliu.safeplate.llm.ClassifyConfig;
import com.jillliu.safeplate.llm.EvidenceCategory;
import com.jillliu.safeplate.llm.EvidenceClassification;

public class EvalReportTest {

    @Test
    void testAllBranches() {

        EvalItem item1 = new EvalItem("t1", EvidenceCategory.POSITIVE, "gluten free options");
        EvalResult r1 = new EvalResult(item1, null, 100, "timeout");

        EvalItem item2 = new EvalItem("t2", EvidenceCategory.POSITIVE, "the menu has clear gluten free label");
        EvidenceClassification c2 = new EvidenceClassification(EvidenceCategory.POSITIVE, "gluten free label",
                "reviwer mentioned gluten free label");
        EvalResult r2 = new EvalResult(item2, c2, 200, null);

        EvalItem item3 = new EvalItem("t3", EvidenceCategory.POSITIVE, "gluten free options");
        EvidenceClassification c3 = new EvidenceClassification(EvidenceCategory.POSITIVE, "safe for celiac",
                "reviews said safe for celiac");
        EvalResult r3 = new EvalResult(item3, c3, 300, null);

        EvalItem item4 = new EvalItem("t4", EvidenceCategory.POSITIVE, "Great place! All of my families love it");
        EvidenceClassification c4 = new EvidenceClassification(EvidenceCategory.IRRELEVANT, "", "");
        EvalResult r4 = new EvalResult(item4, c4, 200, null);

        List<EvalResult> results = List.of(r1, r2, r3, r4);
        ClassifyConfig evalConfig = new ClassifyConfig("model", "v1", true, 0.0);

        EvalReport report = EvalReport.from(evalConfig, results);
        assertEquals(1, report.errorCount());
        assertEquals(1, report.badQuoteCount());
        assertEquals(2, report.correctCount());
        assertEquals(4, report.total());
        assertEquals(1, report.wrongResults().size());
        assertEquals(2.0 / 3, report.accuracy(), 1e-9);
        assertEquals(200, report.averageLatencyMillis(), 1e-9);
        assertEquals(2, report.confusion()[EvidenceCategory.POSITIVE.ordinal()][EvidenceCategory.POSITIVE.ordinal()]);
        assertEquals(1, report.confusion()[EvidenceCategory.POSITIVE.ordinal()][EvidenceCategory.IRRELEVANT.ordinal()]);
        assertEquals(1, report.errorResults().size());
    }

}
