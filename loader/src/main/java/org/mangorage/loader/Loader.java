package org.mangorage.loader;

import com.google.gson.Gson;
import org.mangorage.loader.internal.JPMSGameClassloader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public final class Loader {
    private static final Gson GSON = new Gson();

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

    /**
     * Extracts a specific file from a JAR and writes it to the given output location.
     *
     * @param jarPath   Path to the jar file
     * @param fileInJar Path inside the jar (e.g., "META-INF/jarjar/json-20240303.jar")
     * @param output    Path where the extracted file should be written
     * @throws IOException if reading/writing fails
     */
    public static void extractFileFromJar(String jarPath, String fileInJar, Path output) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath)) {
            JarEntry entry = jarFile.getJarEntry(fileInJar);
            if (entry == null) {
                throw new FileNotFoundException("File not found in JAR: " + fileInJar);
            }

            // Create parent dirs for output if needed
            Files.createDirectories(output.getParent());

            try (InputStream in = jarFile.getInputStream(entry);
                 OutputStream out = Files.newOutputStream(output, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                in.transferTo(out);
            }
        }
    }


    public static void init(String[] args, ModuleLayer parent) throws IOException, InterruptedException {
        System.out.println("Booted into JPMS successfully, booting into game now!");

        List<Path> mods = new ArrayList<>();
        mods.addAll(findJarsInFolder(Path.of("classpath\\game")));
        mods.addAll(findJarsInFolder(Path.of("mods")));

        mods.forEach(mod -> {
            try {
                var modMetadata = new String(fetchFile(
                        mod.toAbsolutePath().toString(),
                        "META-INF/jarjar/metadata.json"
                ));

                final var jarConfig = GSON.fromJson(modMetadata, JarConfig.class);
                jarConfig.getJars()
                        .forEach(jar -> {
                            try {
                                extractFileFromJar(
                                        mod.toString(),
                                        jar.getPath(),
                                        Path.of("classpath").resolve("libraries").resolve(jar.getPath().replaceFirst("META-INF/jarjar", "").substring(1))
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

            } catch (IOException e) {
                System.out.println("Didnt find any jarJars for " + mod);
            }
        });

        final var librariesPath = Path.of("classpath\\libraries");
        final List<Path> libraries = Files.exists(librariesPath) ? findJarsInFolder(librariesPath) : List.of();
        final List<Configuration> parents = new ArrayList<>();
        parents.add(parent.configuration());



        final var moduleCfg = Configuration.resolveAndBind(
                ModuleFinder.of(libraries.toArray(Path[]::new)),
                parents,
                ModuleFinder.of(mods.toArray(Path[]::new)),
                Set.of()
        );

        final var classloader = new JPMSGameClassloader(
                moduleCfg.modules(),
                Thread.currentThread().getContextClassLoader()
        );

        final var moduleLayerController = ModuleLayer.defineModules(moduleCfg, List.of(parent), s -> classloader);
        final var moduleLayer = moduleLayerController.layer();

        Thread.currentThread().setContextClassLoader(classloader);

        classloader.load(moduleLayer, moduleLayerController);

        ModLoader.loadMods(moduleLayer);
    }
}
