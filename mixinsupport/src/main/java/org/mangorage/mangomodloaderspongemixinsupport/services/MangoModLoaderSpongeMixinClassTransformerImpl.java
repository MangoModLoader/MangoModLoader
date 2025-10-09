package org.mangorage.mangomodloaderspongemixinsupport.services;

import org.mangorage.mangomodloaderspongemixinsupport.mixin.MangoModLoaderSpongeMixinImpl;
import org.mangorage.loader.api.IClassTransformer;
import org.mangorage.loader.api.TransformResult;
import org.mangorage.loader.api.TransformerFlag;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.List;

public final class MangoModLoaderSpongeMixinClassTransformerImpl implements IClassTransformer {

    private final List<String> blacklisted = List.of(
            "java.",
            "org.spongepowered",
            "org.objectweb"
    );

    public MangoModLoaderSpongeMixinClassTransformerImpl() {
        MangoModLoaderSpongeMixinImpl.load();
    }

    @Override
    public TransformResult transform(String name, byte[] bytes) {
        for (String s : blacklisted) {
            if (name.startsWith(s)) {
                return new TransformResult(null, TransformerFlag.NO_REWRITE);
            }
        }

        var transformer = MangoModLoaderSpongeMixinImpl.getTransformer();

        var transformed = transformer.transformClass(
                MixinEnvironment.getCurrentEnvironment(),
                name,
                bytes
        );

        if (!areByteArraysEqual(transformed, bytes)) {
            return new TransformResult(transformed, TransformerFlag.FULL_REWRITE);
        } else {
            return new TransformResult(null, TransformerFlag.NO_REWRITE);
        }
    }

    public static boolean areByteArraysEqual(byte[] a, byte[] b) {
        if (a == b) return true; // same reference, duh
        if (a == null || b == null) return false; // null? get out
        if (a.length != b.length) return false; // different size = automatic failure

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false; // mismatch? trash it
        }

        return true; // you got lucky, they're equal
    }

    @Override
    public String getName() {
        return "org/mangorage/mangomodloaderspongemixinsupport/mixin";
    }
}
