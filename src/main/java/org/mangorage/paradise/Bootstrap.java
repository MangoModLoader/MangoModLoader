package org.mangorage.paradise;

import org.mangorage.paradise.core.loader.JPMSGameClassloader;
import org.mangorage.paradise.game.GameImpl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Bootstrap {

    public record Library(String url, String jarName) {}

    private static final List<Library> libraries = new ArrayList<>();

    static {
        libraries.add(new Library(
                "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar",
                "gson.jar"
        ));
    }

    public static void main(String[] args) throws Exception {
        final Path gameLibraries = Path.of("libraries");

        // make sure the folder exists
        if (!Files.exists(gameLibraries)) {
            Files.createDirectories(gameLibraries);
        }

        // check/download each library
        for (Library lib : libraries) {
            Path targetFile = gameLibraries.resolve(lib.jarName);

            if (!Files.exists(targetFile)) {
                System.out.println("Downloading " + lib.jarName + "...");
                downloadFile(lib.url(), targetFile);
                System.out.println("Downloaded " + lib.jarName);
            } else {
                System.out.println(lib.jarName + " already exists, skipping...");
            }
        }


        List<URL> jars = new ArrayList<>();

        final var codeSource = GameImpl.class.getProtectionDomain().getCodeSource();
        jars.add(codeSource.getLocation());

        libraries.forEach(library -> {
            try {
                jars.add(
                        gameLibraries.resolve(library.jarName).toUri().toURL()
                );
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        final var parent = ModuleLayer.boot();
        final var moduleCfg = Configuration.resolve(
                ModuleFinder.of(
                        jars.stream()
                                .map(url -> {
                                    try {
                                        return Path.of(url.toURI());
                                    } catch (URISyntaxException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                                .toArray(Path[]::new)
                ),
                List.of(
                        parent.configuration()
                ),
                ModuleFinder.of(

                ),
                Set.of(
                        "minersparadise"
                )
        );

        var classloader = new JPMSGameClassloader(moduleCfg.modules(), Thread.currentThread().getContextClassLoader().getParent());
        final var moduleLayerController = ModuleLayer.defineModules(moduleCfg, List.of(parent), s -> classloader);
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


    private static void downloadFile(String url, Path target) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}