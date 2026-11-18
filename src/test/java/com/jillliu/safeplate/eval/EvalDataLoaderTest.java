package com.jillliu.safeplate.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.jillliu.safeplate.llm.EvidenceCategory;

public class EvalDataLoaderTest {

    @Test
    void loadSampleFile() throws IOException {
        EvalDataLoader loader = new EvalDataLoader();
        Path file = Path.of("src/test/resources/evidence-eval-sample.jsonl");
        List<EvalItem> items = loader.load(file);
        assertEquals(EvidenceCategory.POSITIVE, items.get(0).expected());
        assertEquals(EvidenceCategory.BOILERPLATE, items.get(2).expected());
        assertEquals("s1", items.get(0).id());
        assertEquals("Great coffee and the patio is lovely in the afternoon.", items.get(3).text());
        assertEquals(4, items.size());
    }

}
