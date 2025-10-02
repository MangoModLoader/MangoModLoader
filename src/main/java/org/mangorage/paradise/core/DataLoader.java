package org.mangorage.paradise.core;

import org.mangorage.paradise.core.game.Game;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class DataLoader {

    public static void initSpritesData() {
        try {
            Thread.currentThread().getContextClassLoader().getResources("assets/sprites").asIterator().forEachRemaining(url -> {
                try {
                    listJsonFiles(url);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void listJsonFiles(URL url) throws IOException, URISyntaxException {
        String protocol = url.getProtocol();

        if (protocol.equals("file")) {
            File file = new File(url.toURI());
            if (file.isDirectory()) {
                try (Stream<Path> stream = Files.walk(file.toPath())) {
                    stream.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> Game.getInstance().getSpriteManager().loadJson(getRelativeResourcePath(p)));
                }
            }
        } else if (protocol.equals("jar")) {
            // url example: jar:file:/path/to/jar.jar!/assets/sprites
            JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            try (JarFile jar = jarConnection.getJarFile()) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
                        Game.getInstance().getSpriteManager().loadJson(entry.getName());
                    }
                }
            }
        }
    }


    /**
     * Given a URL pointing somewhere in resources, returns the relative path
     * starting from the first folder named "assets" (or whatever marker you want).
     */
    public static String getRelativeResourcePath(Path fullPath) {
        // Find "assets" in the path
        for (int i = 0; i < fullPath.getNameCount(); i++) {
            if (fullPath.getName(i).toString().equals("assets")) {
                // Take subpath from "assets" to the very last element (includes file name)
                Path relative = fullPath.subpath(i, fullPath.getNameCount());
                return relative.toString().replace("\\", "/"); // always forward slashes
            }
        }

        // fallback: just the file name
        return fullPath.getFileName().toString();
    }

}
