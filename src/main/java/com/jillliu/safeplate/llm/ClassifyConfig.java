package com.jillliu.safeplate.llm;

public record ClassifyConfig(
        String model,
        String promptVersion,
        boolean think,
        double temperature) {

}
