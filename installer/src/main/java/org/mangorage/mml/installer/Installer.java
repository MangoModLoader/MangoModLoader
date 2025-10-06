package org.mangorage.mml.installer;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Installer {
    /**
     * Extracts files from a JAR, returns a list of extracted JAR file paths.
     *
     * @param jarPath   Path to the source JAR
     * @param folder    Folder inside the JAR to extract (e.g., "assets/"), or null for everything
     * @param outputDir Output directory where files will be extracted
     * @return List of Paths to extracted JAR files
     * @throws IOException
     */
    public static List<Path> extractJarsFromJar(Path jarPath, String folder, Path outputDir) throws IOException {
        List<Path> foundJars = new ArrayList<>();

        if (folder != null && !folder.endsWith("/")) {
            folder = folder + "/";
        }

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // skip anything not in the folder if specified
                if (folder != null && !name.startsWith(folder)) continue;

                Path outFile = outputDir.resolve(name);

                if (!entry.isDirectory()) {
                    // ensure parent directories exist
                    Files.createDirectories(outFile.getParent());

                    // extract file
                    try (InputStream in = jarFile.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(outFile)) {
                        in.transferTo(out);
                    }

                    // collect extracted JARs
                    if (name.endsWith(".jar")) {
                        foundJars.add(outFile);
                    }
                }
            }
        }

        return foundJars;
    }

    public static Properties readProperties(Path filePath) {
        Properties props = new Properties();
        try {
            props.load(Files.newInputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    public static String readFileToString(Path filePath) {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Return empty string if something goes wrong
        }
    }


    /**
     * Copies a file to a new location with a new name.
     *
     * @param source      Path to the source file
     * @param targetDir   Path to the target directory
     * @param newName     New file name (including extension)
     * @throws IOException if something goes wrong
     */
    public static void copyFileWithNewName(Path source, Path targetDir, String newName) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path target = targetDir.resolve(newName);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }


    /**
     * Writes the given string content to a file.
     * If the parent directories do not exist, they will be created.
     *
     * @param filePath The path to the file to write to.
     * @param content  The string content to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void writeStringToFile(Path filePath, String content) throws IOException {
        // Ensure parent directories exist
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Write the content
        Files.writeString(filePath, content);
    }

    /*
    Boot.jar

    versions.json (for minecraft launcher)
    versionInfo.properties (for the loader itself?)


    Put bootJar in libraries/org/mangorage/boot/boot-{mc-version}-{loader_version}.jar
    Put gameJar in libraries/org/mangorage/minecraft/minecraft-{mc-version}.jar
     */
    public static void installClient(String mcHome) {
        System.out.println("Installer Ran");

        try {
            System.out.println(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

            // Should always be 2, boot/mavenizer
            final var jarsExtracted = extractJarsFromJar(
                    Path.of(Installer.class.getProtectionDomain().getCodeSource().getLocation().toURI()),
                    "META-INF",
                    Path.of("output/jar-resources")
            );

            final var mavenizerJar = jarsExtracted
                    .stream()
                    .filter(path -> path.getFileName().toString().contains("mavenizer"))
                    .findAny().get();

            final var loaderJar = jarsExtracted
                    .stream()
                    .filter(path -> path.getFileName().toString().contains("loader"))
                    .findAny().get();

            final var versionsJson = Path.of("output/jar-resources/META-INF/data/versions.json");
            final var buildInfo = Path.of("output/jar-resources/META-INF/internal/mangomodloader/buildInfo.properties");

            System.out.println("Found -> " + mavenizerJar.toAbsolutePath());
            System.out.println("Found -> " + loaderJar.toAbsolutePath());
            System.out.println("Found -> " + versionsJson.toAbsolutePath());
            System.out.println("Found -> " + buildInfo.toAbsolutePath());

            // TODO: Make sure we dont already got a version file generated and then if we do, prefix the id with a number...

            final var buildInfoProperties = readProperties(buildInfo);
            final var versionsJsonData = readFileToString(versionsJson);

            final var mcVersion = buildInfoProperties.getProperty("mc_version").replaceAll("\"", "");
            System.out.println(mcVersion);
            final var loaderVersion = buildInfoProperties.getProperty("version");

            final var bootName = "boot-" + mcVersion + "-" +  loaderVersion + ".jar";
            final var bootVersion = mcVersion + "-" +  loaderVersion;

            copyFileWithNewName(
                    loaderJar,
                    Path.of(mcHome).resolve("libraries").resolve("org/mangorage/boot/" + bootVersion + "/"),
                    bootName
            );

            // TODO: Generate MC Jar and move/rename into libraries/org/mangorage/minecraft/
            JarRunner.generate(mavenizerJar, mcVersion);

            final var joinedJar = Path.of("output").resolve(JarRunner.resolveJoinedJar(mcVersion));

            System.out.println("Found -> " + joinedJar.toAbsolutePath());

            final var minecraftName = "minecraft-" + mcVersion + "-" +  loaderVersion + ".jar";
            final var minecraftVersion = mcVersion + "-" +  loaderVersion;

            copyFileWithNewName(
                    joinedJar,
                    Path.of(mcHome).resolve("libraries").resolve("org/mangorage/minecraft/" + minecraftVersion + "/"),
                    minecraftName
            );

            // Write the versions File
            final var versionJsonData =
                    versionsJsonData
                            .replace("{ID}", "mangoloader-" + mcVersion + "-" + loaderVersion)
                            .replace("{MC_VERSION}", mcVersion)
                            .replace("{RELEASE_TIME}", buildInfoProperties.getProperty("buildTime"))
                            .replace("{TIME}", buildInfoProperties.getProperty("buildTime")) // TODO: FIX LATER
                            .replace("{wrapper}", "org.mangorage:boot:" + mcVersion + "-" + loaderVersion);

            System.out.println(versionJsonData);

            final var name = "mangoloader-" + mcVersion + "-" + loaderVersion;
            writeStringToFile(
                    Path.of(mcHome)
                            .resolve("versions/" + name)
                            .resolve(name + ".json"),
                    versionJsonData
            );
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InstallerGui::createAndShowGUI);
    }
}
