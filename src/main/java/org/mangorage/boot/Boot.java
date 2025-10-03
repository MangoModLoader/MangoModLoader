package org.mangorage.boot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 *
 * Extract Out GSON
 *
 * Boot into module Layer "loader" which will create layer game with all the libraries/mods
 *
 * Loader layer has one module hardcoded → GSON/loader
 *
 * Go through mods and extract out the libraries
 *
 *
 * classpath
 *  -> loader
 *   -> gson/loader.jar
 *  -> game
 *      libraries
 *
 * mods
 *  -> go thru and extract libraries into classpath/libraries
 */
public final class Boot {
    // Copy jars into folders
    public static void extractJars(Path sourceJar, List<MetadataInfo> jars, Path outputRoot) throws IOException {
        try (JarFile jarFile = new JarFile(sourceJar.toFile())) {
            for (MetadataInfo info : jars) {
                String internalPath = info.jar(); // e.g., META-INF/jarjar/game-1.0-SNAPSHOT.jar
                ZipEntry entry = jarFile.getEntry(internalPath);
                if (entry == null) {
                    System.err.println("Jar not found inside source: " + internalPath);
                    continue;
                }

                // Create output folder: outputRoot/layer/
                Path layerFolder = outputRoot.resolve(info.layer());
                Files.createDirectories(layerFolder);

                // Output jar path
                Path outJar = layerFolder.resolve(Paths.get(internalPath).getFileName());

                try (InputStream in = jarFile.getInputStream(entry);
                     OutputStream out = Files.newOutputStream(outJar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    in.transferTo(out);
                }

                System.out.println("Extracted Jar -> " + outJar);
            }
        }
    }

    public static void deleteFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) return; // nothing to delete

        Files.walkFileTree(folder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); // delete each file
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); // delete the directory after its contents
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void callInit(String className, String[] args, Module module, ModuleLayer moduleLayer) {
        try {
            Class<?> clazz = Class.forName(module, className);
            Method initMethod = clazz.getMethod("init", String[].class, ModuleLayer.class);

            // Make sure it’s static and public
            if (!java.lang.reflect.Modifier.isStatic(initMethod.getModifiers())) {
                throw new IllegalStateException("init method is not static, are you high?");
            }

            // Invoke the init method
            initMethod.invoke(null, args, moduleLayer);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't reflectively call init because something exploded.", e);
        }
    }

    /**
     * Fetches the contents of a file inside a JAR.
     *
     * @param jarPath   Path to the JAR file on disk.
     * @param filePath  Exact path inside the JAR (e.g. "assets/game/config.json").
     * @return byte[] with the file’s contents.
     * @throws IOException if not found or failed to read.
     */
    public static byte[] fetchFile(String jarPath, String filePath) throws IOException {
        try (JarFile jar = new JarFile(jarPath)) {
            ZipEntry entry = jar.getEntry(filePath);
            if (entry == null) {
                throw new IOException("File not found in jar: " + filePath);
            }

            try (InputStream in = jar.getInputStream(entry)) {
                return in.readAllBytes();
            }
        }
    }

    // Parser method
    public static List<MetadataInfo> parseGroupsAndPaths(String json) {
        List<MetadataInfo> jars = new ArrayList<>();
        int arrayStart = json.indexOf("\"jars\"");
        if (arrayStart == -1) return jars;

        int index = json.indexOf("[", arrayStart) + 1;
        while (true) {
            int objStart = json.indexOf("{", index);
            if (objStart == -1) break;

            // group
            int groupIndex = json.indexOf("\"group\"", objStart);
            if (groupIndex == -1) break;
            int colonIndex = json.indexOf(":", groupIndex);
            int quoteStart = json.indexOf("\"", colonIndex) + 1;
            int quoteEnd = json.indexOf("\"", quoteStart);
            String group = json.substring(quoteStart, quoteEnd);

            // path
            int pathIndex = json.indexOf("\"path\"", groupIndex);
            if (pathIndex == -1) break;
            colonIndex = json.indexOf(":", pathIndex);
            quoteStart = json.indexOf("\"", colonIndex) + 1;
            quoteEnd = json.indexOf("\"", quoteStart);
            String path = json.substring(quoteStart, quoteEnd);

            jars.add(new MetadataInfo(group, path));

            index = quoteEnd; // move past this object
        }

        return jars;
    }


    public static void main(String[] args) throws IOException, URISyntaxException {
        deleteFolder(Path.of("classpath"));

        final var jar = Boot.class.getProtectionDomain().getCodeSource().getLocation();


        byte[] metadata = fetchFile(
                jar.toURI().getPath(),
                "META-INF/jarjar/metadata.json"
        );

        System.out.println("Found Metadata, size: " + metadata.length);

        String json = new String(metadata);

        final var jars = parseGroupsAndPaths(json);


        extractJars(
                Path.of(Boot.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
                jars,
                Path.of("classpath")
        );

        final var parent = ModuleLayer.boot();
        final var loaderJar = jars.stream()
                .filter(info -> info.layer().contains("loader"))
                .filter(info -> info.jar().contains("loader"))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find loader"));
        final var gsonJar = jars.stream()
                .filter(info -> info.layer().contains("loader"))
                .filter(info -> info.jar().contains("gson"))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find loader"));


        final var moduleCfg = Configuration.resolveAndBind(
                ModuleFinder.of(
                        gsonJar.resolve(Path.of("classpath"))
                ),
                List.of(
                        parent.configuration()
                ),
                ModuleFinder.of(
                        loaderJar.resolve(Path.of("classpath"))
                ),
                Set.of("loader")
        );

        final var classloader = new URLClassLoader(
                new URL[]{loaderJar.resolve(Path.of("classpath")).toUri().toURL()},
                Thread.currentThread().getContextClassLoader()
        );

        final var moduleLayerController = ModuleLayer.defineModulesWithOneLoader(moduleCfg, List.of(parent), classloader);
        final var moduleLayer = moduleLayerController.layer();
        final var module = moduleLayer.findModule("loader").get();

        Thread.currentThread().setContextClassLoader(classloader);

        callInit(
                "org.mangorage.loader.Loader",
                args,
                module,
                moduleLayer
        );
    }
}
