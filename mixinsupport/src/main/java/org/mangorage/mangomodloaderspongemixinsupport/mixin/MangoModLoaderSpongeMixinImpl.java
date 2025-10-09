package org.mangorage.mangomodloaderspongemixinsupport.mixin;


import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.mangorage.mangomodloaderspongemixinsupport.mixin.core.MangoModLoaderMixinServiceImpl;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;

import java.lang.reflect.Method;

public final class MangoModLoaderSpongeMixinImpl {
    private static final boolean DEBUG = false;

    private static boolean loaded = false;
    private static IMixinTransformerFactory factory;
    private static IMixinTransformer transformer;

    public static void setFactory(IMixinTransformerFactory factory) {
        if (MangoModLoaderSpongeMixinImpl.factory != null) return;
        MangoModLoaderSpongeMixinImpl.factory = factory;
    }

    public static void prepare() {
        if (MangoModLoaderSpongeMixinImpl.transformer != null) return;
        MangoModLoaderSpongeMixinImpl.transformer = factory.createTransformer();
    }

    public static IMixinTransformer getTransformer() {
        return transformer;
    }

    public static void load() {
        if (loaded) return;
        loaded = true;

        // Load

        if (DEBUG) {
            System.setProperty("mixin.debug.verbose", "true");
            System.setProperty("mixin.debug", "true");
            System.setProperty("mixin.env.disableRefMap", "true");
            System.setProperty("mixin.checks", "true");
        }

        System.setProperty("mixin.service", MangoModLoaderMixinServiceImpl.class.getName());

        MixinBootstrap.init();

        completeMixinBootstrap();

        MixinExtrasBootstrap.init();
    }

    private static void completeMixinBootstrap() {
        // Move to the default phase.
        try {
            final Method method = MixinEnvironment.class.getDeclaredMethod("gotoPhase", MixinEnvironment.Phase.class);
            method.setAccessible(true);
            method.invoke(null, MixinEnvironment.Phase.INIT);
            method.invoke(null, MixinEnvironment.Phase.DEFAULT);
        } catch(final Exception exception) {
            exception.printStackTrace();
        }
        prepare();
    }
}


