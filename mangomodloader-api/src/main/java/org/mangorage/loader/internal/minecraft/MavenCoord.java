package org.mangorage.loader.internal.minecraft;

import net.minecraftforge.util.data.OS;
import net.minecraftforge.util.data.json.MinecraftVersion;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class MavenCoord {


    /**
     * Converts a MinecraftVersion object into a list of MavenCoords representing all required libraries.
     */
    public static List<Path> fromMinecraftVersion(MinecraftVersion version) {
        final var os = OS.CURRENT;

        return Arrays.stream(version.libraries)
                .filter(library -> {
                    if (library.name.contains("gson")) {
                        return false;
                    } else if (library.rules == null || library.rules.isEmpty()) {
                        return true;
                    } else {
                        for (MinecraftVersion.Rules rule : library.rules) {
                            if (rule.os.toOS() == os) {
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
}
