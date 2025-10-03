package org.mangorage.loader.api;

public record TransformResult(byte[] classData, TransformerFlag flag) {
    public static TransformResult none(byte[] classData) {
        return new TransformResult(classData, TransformerFlag.NO_REWRITE);
    }
}