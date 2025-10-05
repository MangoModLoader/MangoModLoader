package org.mangorage.loader.internal.minecraft;

import net.minecraftforge.util.data.OS;
import net.minecraftforge.util.data.json.MinecraftVersion;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public record MavenCoord(
        String group,
        String artifact,
        String variant,  // can be null if no classifier
        String version,
        Path relativePath
) {
    /**
     * Converts a MinecraftVersion object into a list of MavenCoords representing all required libraries.
     */
    public static List<Path> fromMinecraftVersion(MinecraftVersion version) {
        return Arrays.stream(version.libraries)
                .filter(library -> {
                    if (library.name.contains("gson")) {
                        return false;
                    } else if (library.rules == null || library.rules.isEmpty()) {
                        return true;
                    } else {
                        for (MinecraftVersion.Rules rule : library.rules) {
                            if (rule.os.toOS() == OS.WINDOWS) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .map(library -> {
                    return getRelativePath(library.name);
                })
                .toList();
    }

    /**
     * Computes the relative path for a Maven-style artifact.
     * For example: com.example:lib:1.2.3:natives-windows ->
     * com/example/lib/1.2.3/lib-1.2.3-natives-windows.jar
     */
    private static Path getMavenRelativePath(String group, String artifact, String version, String classifier) {
        String groupPath = group.replace('.', '/');
        String fileName = artifact + "-" + version + (classifier != null ? "-" + classifier : "") + ".jar";
        return Paths.get(groupPath, artifact, version, fileName);
    }

    private static String convert(String coord) {
        if (coord.contains(".")) {
            return coord.replaceAll("\\.", "/");
        }
        return coord;
    }


    private static Path getRelativePath(String name) {
        String[] parts = name.split(":");
        if (parts.length == 3) {
            return Path.of(convert(parts[0])).resolve(parts[1]).resolve(parts[2]).resolve(parts[1] + "-" + parts[2] + ".jar");
        } else if (parts.length == 4) {
            return Path.of(convert(parts[0])).resolve(parts[1]).resolve(parts[2]).resolve(parts[1] + "-" + parts[2] + "-" + parts[3] + ".jar");
        }
        return null; // Should never be null!
    }

    public static void main(String[] args) {
        getRelativePath("com.mojang:jtracy:1.0.36:natives-macos");
    }
}
