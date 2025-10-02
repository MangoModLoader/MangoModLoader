package org.mangorage.boot.loader.api;

public record TransformResult(byte[] classData, TransformerFlag flag) {}