package org.mangorage.example;

import org.mangorage.loader.api.IClassTransformer;
import org.mangorage.loader.api.TransformResult;

public class MyClassTransformer implements IClassTransformer {
    @Override
    public TransformResult transform(String className, byte[] classData) {
        System.out.println("Class -> " + className);
        return TransformResult.none(classData);
    }

    @Override
    public String getName() {
        return "";
    }
}
