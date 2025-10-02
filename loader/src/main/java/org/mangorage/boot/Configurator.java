package org.mangorage.boot;

import org.mangorage.boot.loader.JPMSGameClassloader;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Configurator {
    private static final Map<String, List<String>> jars = new HashMap<>();

    public static void addJar(String jar, String layer) {
        jars.computeIfAbsent(layer, l -> new ArrayList<>()).add(jar);
    }

    public static void init(String[] args, Module parent) {

        final var libraries = jars.getOrDefault("library", new ArrayList<>());
        final var game = jars.getOrDefault("game", new ArrayList<>());
        if (game.isEmpty())
            throw new IllegalStateException("Cannot find game");


        final var gameJars = new ArrayList<>(game.stream()
                .map(Path::of)
                .toList()
        );

        jars.getOrDefault("mod", List.of())
                .forEach(info -> {
                    gameJars.add(Path.of(info));
                });



        final var moduleCfg = Configuration.resolveAndBind(
                ModuleFinder.of(
                        libraries.stream()
                                .map(Path::of)
                                .toArray(Path[]::new)
                ),
                List.of(
                        parent.getLayer().configuration()
                ),
                ModuleFinder.of(gameJars.toArray(Path[]::new)),
                Set.of("minersparadise")
        );

        var classloader = new JPMSGameClassloader(moduleCfg.modules(), Thread.currentThread().getContextClassLoader().getParent());
        final var moduleLayerController = ModuleLayer.defineModulesWithManyLoaders(moduleCfg, List.of(parent.getLayer()), classloader);
        final var moduleLayer = moduleLayerController.layer();

        Thread.currentThread().setContextClassLoader(classloader);

        classloader.load(moduleLayer, moduleLayerController);

        callMain("org.mangorage.paradise.game.GameImpl", args, moduleLayer.findModule("minersparadise").get());
    }

    public static void callMain(String className, String[] args, Module module) {
        try {
            Class<?> clazz = Class.forName(className, false, module.getClassLoader());
            Method mainMethod = clazz.getMethod("main", String[].class);

            // Make sure it's static and public
            if (!java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())) {
                throw new IllegalStateException("Main method is not static, are you high?");
            }

            // Invoke the main method with a godawful cast
            mainMethod.invoke(null, (Object) args);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't reflectively call main because something exploded.", e);
        }
    }
}
