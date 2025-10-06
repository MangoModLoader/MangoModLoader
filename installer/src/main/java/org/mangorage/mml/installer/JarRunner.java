package org.mangorage.mml.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class JarRunner {

    /**
     * Runs a jar file with the specified arguments.
     *
     * @param jarPath Path to the jar file
     * @param args    Arguments to pass to the jar
     * @throws IOException          If an I/O error occurs
     * @throws InterruptedException If the process is interrupted
     */
    public static void runJar(Path jarPath, List<String> args) throws IOException, InterruptedException {
        if (!jarPath.toFile().exists()) {
            throw new IOException("Jar file does not exist: " + jarPath);
        }

        // Build the command: java -jar jarPath arg1 arg2 ...
        List<String> command = new java.util.ArrayList<>();
        command.add("java");
        command.add("-jar");
        command.add(jarPath.toAbsolutePath().toString());
        command.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // merge stdout and stderr
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        System.out.println("Process exited with code: " + exitCode);
    }

    public static void generate(Path mavenizer, String version) {
        try {
            runJar(mavenizer, List.of(
                    "--maven",
                    "--cache", Path.of("output/mavenizer/cache").toAbsolutePath().toString(),
                    "--output", Path.of("output/mavenizer/output").toAbsolutePath().toString(),
                    "--artifact", "net.minecraft:joined",
                    "--version", version
            ));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path resolveJoinedJar(String version) {
        return Path.of("mavenizer/output/net/minecraft/joined/VERSION/joined-VERSION.jar".replaceAll("VERSION", version));
    }
}
