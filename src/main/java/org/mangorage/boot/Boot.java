package org.mangorage.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
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

    // Extracts multiple jars into folders by delegating to copy()
    public static void extractJars(Path sourceJar, List<MetadataInfo> jars, Path outputRoot) throws IOException {
        for (MetadataInfo info : jars) {
            // Output folder for this layer
            Path layerFolder = outputRoot.resolve(info.layer());
            Files.createDirectories(layerFolder);

            // Output file path
            Path outJar = layerFolder.resolve(Paths.get(info.jar()).getFileName());

            try {
                copy(sourceJar, info.jar(), outJar);
                System.out.println("Extracted Jar -> " + outJar);
            } catch (IOException e) {
                System.err.println("Failed to extract " + info.jar() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Copies a file stored inside a fat/shadow jar as a resource to disk.
     *
     * @param containerJar The container jar file on disk
     * @param internalPath Path inside the jar (forward slashes, e.g., "internal/gson-2.11.0.jar")
     * @param output Path to write the extracted file
     * @throws IOException
     */
    public static void copy(Path containerJar, String internalPath, Path output) throws IOException {
        // Open the container jar as a FileSystem
        try (FileSystem fs = FileSystems.newFileSystem(containerJar)) {
            Path resource = fs.getPath(internalPath.replace("\\", "/"));
            if (!Files.exists(resource)) {
                throw new FileNotFoundException("Internal jar not found: " + internalPath);
            }

            // Make parent dirs
            if (output.getParent() != null) Files.createDirectories(output.getParent());

            Files.copy(resource, output, StandardCopyOption.REPLACE_EXISTING);
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
            Class<?> clazz = Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            Method initMethod = clazz.getMethod("init", String[].class, ModuleLayer.class);

            // Make sure it’s static and public
            if (!java.lang.reflect.Modifier.isStatic(initMethod.getModifiers())) {
                throw new IllegalStateException("init method is not static!");
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
        System.out.println("Booted Game!");
        deleteFolder(Path.of("classpath"));

        final var jar = Boot.class.getProtectionDomain().getCodeSource().getLocation();

        System.out.println(jar);

        byte[] metadata = fetchFile(
                jar.toURI().getPath(),
                "META-INF/jarjar/metadata.json"
        );

        System.out.println("Found Metadata, size: " + metadata.length);

        String json = new String(metadata);

        final var jars = parseGroupsAndPaths(json);
        final var gsonJar = Path.of("classpath").resolve("loader").resolve("gson.jar").toAbsolutePath();
        final var utilJar = Path.of("classpath").resolve("loader").resolve("util.jar").toAbsolutePath();

        copy(
                Path.of(Boot.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
                "internal/gson-2.11.0.jar",
                gsonJar
        );

        copy(
                Path.of(Boot.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
                "internal/json-data-utils-0.2.3.jar",
                utilJar
        );

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


        final var moduleCfg = Configuration.resolveAndBind(
                ModuleFinder.of(
                        gsonJar, utilJar
                ),
                List.of(
                        parent.configuration()
                ),
                ModuleFinder.of(
                        loaderJar.resolve(Path.of("classpath"))
                ),
                Set.of("loader", "com.google.gson", "net.minecraftforge.utils.json_data")
        );

        final List<URL> urls = new ArrayList<>();
        urls.add(loaderJar.resolve(Path.of("classpath")).toUri().toURL());
        urls.add(gsonJar.toUri().toURL());
        urls.add(utilJar.toUri().toURL());

        final var classloader = new URLClassLoader(
                urls.toArray(URL[]::new),
                Thread.currentThread().getContextClassLoader()
        );

        final var moduleLayerController = ModuleLayer.defineModules(moduleCfg, List.of(parent), (s) -> classloader);

        final var moduleLayer = moduleLayerController.layer();
        final var module = moduleLayer.findModule("loader").get();
        final var ok = moduleLayer.findModule("com.google.gson");

        System.out.println(ok.get().getName());

        Thread.currentThread().setContextClassLoader(classloader);

        callInit(
                "org.mangorage.loader.Loader",
                args,
                module,
                moduleLayer
        );
    }
}
