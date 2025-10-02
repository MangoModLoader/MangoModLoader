package org.mangorage.boot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class Boot {

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

    public static List<Path> findJarsInFolder(Path folder) throws IOException {
        List<Path> jars = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry) && entry.getFileName().toString().toLowerCase().endsWith(".jar")) {
                    jars.add(entry);
                }
            }
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

        Map<String, List<MetadataInfo>> layers = new HashMap<>();
        jars.forEach(info -> {
            layers.computeIfAbsent(info.layer(), id -> new ArrayList<>()).add(info);
        });

        extractJars(
                Path.of(Boot.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
                jars,
                Path.of("classpath")
        );


        findJarsInFolder(Path.of("mods").toAbsolutePath()).forEach(mod -> {
            try {
                final var modMetadata = fetchFile(
                        mod.toString(),
                        "META-INF/jarjar/metadata.json"
                );

                final var jarsInMod = parseGroupsAndPaths(new String(modMetadata));

                extractJars(
                        mod,
                        jarsInMod,
                        Path.of("classpath")
                );

                jars.addAll(jarsInMod);

                jars.add(new MetadataInfo("mod", mod.toString()));
            } catch (IOException e) {
                System.out.println("Unable to find any JarInJars for mod jar " + mod);
            }
        });




        final var parent = ModuleLayer.boot();
        final var loaderJar = layers.get("loader").getFirst().resolve(Path.of("classpath").toAbsolutePath());

        final var moduleCfg = Configuration.resolveAndBind(
                ModuleFinder.of(
                        loaderJar
                ),
                List.of(
                        parent.configuration()
                ),
                ModuleFinder.of(),
                Set.of("loader")
        );

        final var classloader = new URLClassLoader(
                new URL[]{loaderJar.toUri().toURL()},
                Thread.currentThread().getContextClassLoader()
        );
        final var moduleLayerController = ModuleLayer.defineModulesWithOneLoader(moduleCfg, List.of(parent), classloader);
        final var moduleLayer = moduleLayerController.layer();
        final var module = moduleLayer.findModule("loader").get();

        Thread.currentThread().setContextClassLoader(classloader);

        jars.forEach(info -> {
            callAddJar(
                    "org.mangorage.boot.Configurator",
                    info.resolve(Path.of("classpath").toAbsolutePath()).toString(),
                    info.layer(),
                    module
            );
        });

        callInit(
                "org.mangorage.boot.Configurator",
                args,
                module
        );
    }

    public static void callAddJar(String className, String arg1, String arg2, Module module) {
        try {
            Class<?> clazz = Class.forName(module, className);
            Method addJarMethod = clazz.getMethod("addJar", String.class, String.class);

            // Make sure it's static and public
            if (!java.lang.reflect.Modifier.isStatic(addJarMethod.getModifiers())) {
                throw new IllegalStateException("addJar method is not static, are you high?");
            }

            // Invoke the method
            addJarMethod.invoke(null, arg1, arg2);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't reflectively call addJar because something exploded.", e);
        }
    }

    public static void callInit(String className, String[] args, Module module) {
        try {
            Class<?> clazz = Class.forName(module, className);
            Method initMethod = clazz.getMethod("init", String[].class, Module.class);

            // Make sure it’s static and public
            if (!java.lang.reflect.Modifier.isStatic(initMethod.getModifiers())) {
                throw new IllegalStateException("init method is not static, are you high?");
            }

            // Invoke the init method
            initMethod.invoke(null, args, module);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't reflectively call init because something exploded.", e);
        }
    }
}
