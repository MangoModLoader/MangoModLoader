package org.mangorage.loader.internal.minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MinecraftGenerator {
    public static Process runJar(Path jar, Path runPath, String[] args) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        // Build the full command
        String[] cmd = new String[args.length + 3];
        cmd[0] = javaBin;
        cmd[1] = "-jar";
        cmd[2] = jar.toAbsolutePath().toString();
        System.arraycopy(args, 0, cmd, 3, args.length);

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.directory(runPath.toFile());
        builder.inheritIO(); // pipes stdout/stderr so you actually see it

        return builder.start();
    }

    public static void generate(String version) {
        final var mavenizer = Path.of("classpath").resolve("mavenizer").resolve("minecraft-mavenizer-0.3.19.jar");

        try {
            if (!Files.exists(Path.of("mavenizer")))
                Files.createDirectories(
                        Path.of("mavenizer")
                );

            final var output = runJar(mavenizer, Path.of("mavenizer"), new String[]{
                    "--maven",
                    "--cache", Path.of("mavenizer").resolve("cache").toAbsolutePath().toString(),
                    "--output", Path.of("mavenizer").resolve("output").toAbsolutePath().toString(),
                    "--artifact", "net.minecraft:joined",
                    "--version", version
            });

            output.waitFor();


        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
