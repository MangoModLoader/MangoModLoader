package org.mangorage.paradise;

import org.mangorage.paradise.core.loader.GameURLClassloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

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

        if (!codeSource.getLocation().getFile().contains(".jar")) {
            final var path = Path.of(codeSource.getLocation().toURI()).getParent().getParent().getParent().resolve("resources/main");
            jars.add(path.toUri().toURL());
        }

        libraries.forEach(library -> {
            try {
                jars.add(
                        gameLibraries.resolve(library.jarName).toUri().toURL()
                );
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        URL[] urls = jars.toArray(URL[]::new);

        final var parent = Thread.currentThread().getContextClassLoader().getParent();
        GameURLClassloader classloader = new GameURLClassloader(urls, parent);

        Thread.currentThread().setContextClassLoader(classloader);

        Class<?> clazz = Class.forName("org.mangorage.paradise.GameImpl", false, classloader);

        // Grab the main method
        Method mainMethod = clazz.getMethod("main", String[].class);

        // Call it with an empty args array (or pass your own args)
        mainMethod.invoke(null, (Object) args);
    }


    private static void downloadFile(String url, Path target) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}