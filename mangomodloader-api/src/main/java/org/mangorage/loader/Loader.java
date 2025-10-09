package org.mangorage.loader;

import com.google.gson.Gson;
import org.mangorage.loader.api.mod.Environment;
import org.mangorage.loader.internal.Constants;
import org.mangorage.loader.internal.JPMSGameClassloader;
import org.mangorage.loader.internal.util.Util;
import org.mangorage.loader.internal.util.WorkingDialog;
import org.mangorage.loader.internal.minecraft.MavenCoord;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.mangorage.loader.internal.minecraft.MinecraftFetcher.fetch;

public final class Loader {
    private static final String GAME_MODULE = "minecraft";

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

    public static Path locateMinecraft() {
        final var locatedJar = Path.of("classpath-game\\minecraft.jar");
        if (Files.exists(locatedJar)) return locatedJar;
        final var minecraftVersion = Constants.MINECRAFT_VERSION + "-" + Constants.LOADER_VERSION;
        return Path.of("libraries/org/mangorage/minecraft/" + minecraftVersion + "/" + "minecraft-" + minecraftVersion + ".jar");
    }

    public static Path getBaseFolder() {
        if (Files.exists(Path.of("libraries"))) {
            return Path.of("");
        }

        String userHome = System.getProperty("user.home") + "\\AppData\\Roaming\\";
        return Path.of(userHome).resolve(".minecraft");
    }

    public static void scanArgs(String[] args) {
        for (String arg : args) {
            if (arg.contains("--mml.server")) {
                Constants.ENV = Environment.SERVER;
            }
        }
    }

    public static void init(String[] args, ModuleLayer parent) throws IOException {
        Constants.ENV = Environment.CLIENT;
        scanArgs(args);

        final var dialog = new WorkingDialog();

        try {
            dialog.init("Starting Game soon!");

            Thread.sleep(4000);

            if (!Files.exists(Path.of("mods"))) {
                Files.createDirectories(Path.of("mods"));
            }

            dialog.setText("Generating Minecraft sources for version " + Constants.MINECRAFT_VERSION);

            final var minecraftFolder = getBaseFolder();

            if (!Files.exists(minecraftFolder)) {
                throw new IllegalStateException("Unable to find .minecraft in users folder");
            }

            dialog.setText("Booted into JPMS successfully, booting into game now!");

            final var MC = fetch(Constants.MINECRAFT_VERSIONS_JSON_URL);
            final var mcLibraries = MavenCoord.fromMinecraftVersion(MC)
                    .stream()
                    .map(path -> minecraftFolder.resolve("libraries").resolve(path))
                    .filter(Files::exists)
                    .toList();

            List<Path> mods = new ArrayList<>();
            System.out.println(locateMinecraft().toAbsolutePath());
            mods.add(locateMinecraft());
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
            final List<Path> libraries = Files.exists(librariesPath) ? findJarsInFolder(librariesPath) : new ArrayList<>();
            libraries.addAll(
                    mcLibraries.stream()
                            .map(path -> minecraftFolder.resolve("libraries").resolve(path))
                            .toList()
            );


            final List<Configuration> parents = new ArrayList<>();
            parents.add(parent.configuration());


            Set<String> moduleNames = new HashSet<>();
            moduleNames.addAll(Util.getModuleNames(Path.of("classpath").resolve("libraries")));
            moduleNames.add(GAME_MODULE);

            mcLibraries.stream()
                    .map(path -> minecraftFolder.resolve("libraries").resolve(path))
                    .forEach(path -> {
                        moduleNames.add(Util.getModuleName(path.toFile()));
                    });

            // sun.security.ec

            final var moduleCfg = Configuration.resolveAndBind(
                    ModuleFinder.of(libraries.toArray(Path[]::new)),
                    parents,
                    ModuleFinder.of(mods.toArray(Path[]::new)),
                    moduleNames
            );

            final var classloader = new JPMSGameClassloader(
                    moduleCfg.modules(),
                    Thread.currentThread().getContextClassLoader()
            );

            final var moduleLayerController = ModuleLayer.defineModules(moduleCfg, List.of(parent), s -> classloader);
            final var moduleLayer = moduleLayerController.layer();

            Thread.currentThread().setContextClassLoader(classloader);

            // Handle Client side add Reads/Opens
            if (Constants.ENV == Environment.CLIENT) {
                final var joinedModule = moduleLayer.findModule(GAME_MODULE).get();
                final var lwjglModule = moduleLayer.findModule("org.lwjgl").get();

                moduleLayerController.addReads(joinedModule, lwjglModule);
                moduleLayerController.addOpens(lwjglModule, "org.lwjgl.system", joinedModule);
            }

            classloader.load(moduleLayer, moduleLayerController);

            ModLoader.loadMods(moduleLayer);

            Class<?> clazz;

            if (Constants.ENV == Environment.CLIENT) {
                clazz = Class.forName("net.minecraft.client.main.Main", false, Thread.currentThread().getContextClassLoader());
                // TODO: Make our built in Gradle plugin responsible for handling args... eventually
                if (args.length == 0)
                    args = new String[] {
                            "--username", "MangoRage",
                            "--version", "MangoModLoader " + Constants.MINECRAFT_VERSION + " Loader " + Constants.LOADER_VERSION,
                            "--assetIndex", "5",
                            "--uuid", "94b5df2e-2b64-10ed-0007-040300000000",
                            "--clientId", "null",
                            "--xuid", "null",
                            "--userType", "mojang",
                            "--versionType", "release",
                            "--width", "925",
                            "--height", "530",
                            "--accessToken", "none"
                    };
            } else {
                clazz = Class.forName("net.minecraft.server.Main", false, Thread.currentThread().getContextClassLoader());
            }

            dialog.setText("Loading Game...");
            dialog.close();

            try {
                clazz.getMethod("main", String[].class).invoke(null, (Object) args);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
