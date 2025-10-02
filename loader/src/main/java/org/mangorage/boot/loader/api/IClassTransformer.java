package org.mangorage.boot.loader.api;

public interface IClassTransformer {
    TransformResult transform(String className, byte[] classData);

    String getName();
}