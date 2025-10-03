package org.mangorage.loader.api;

public interface IClassTransformer {
    TransformResult transform(String className, byte[] classData);
    String getName();
}