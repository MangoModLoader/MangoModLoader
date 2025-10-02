package org.mangorage.boot;

import java.nio.file.Path;

public record MetadataInfo(String layer, String jar) {
    public static String afterJarjar(String path) {
        String marker = "jarjar/";
        int index = path.indexOf(marker);
        if (index == -1) {
            return null; // or path itself if you prefer
        }
        return path.substring(index + marker.length());
    }

    public Path resolve(Path path) {
        return path.resolve(layer).resolve(afterJarjar(jar));
    }
}
