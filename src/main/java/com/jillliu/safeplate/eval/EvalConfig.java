package com.jillliu.safeplate.eval;

public record EvalConfig(
        String model,
        String promptVersion,
        boolean think,
        double temperature) {

}
