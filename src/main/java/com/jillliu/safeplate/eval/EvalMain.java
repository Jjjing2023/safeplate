package com.jillliu.safeplate.eval;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.jillliu.safeplate.llm.ClassifyConfig;
import com.jillliu.safeplate.llm.OllamaClient;

public class EvalMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        ClassifyConfig config = new ClassifyConfig("qwen3:8b", "v3", false, 0.0);
        EvalDataLoader evalDataLoader = new EvalDataLoader();
        EvalRunner evalRunner = new EvalRunner(new OllamaClient(config));
        Path file = Path.of(System.getProperty("user.home"), "safeplate-data", "evidence-eval.jsonl");
        List<EvalItem> items = evalDataLoader.load(file);
        List<EvalResult> results = evalRunner.run(items);
        EvalReport report = EvalReport.from(config, results);
        System.out.println(report.format());
    }

}
