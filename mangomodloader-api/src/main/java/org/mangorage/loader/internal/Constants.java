package org.mangorage.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Constants {
    public static final String MINECRAFT_VERSION = "1.21.9";
    public static final String MINECRAFT_VERSIONS_JSON_URL = "https://piston-meta.mojang.com/v1/packages/d7a33415a8e68a8fdff87ab2020e64de021df302/1.21.9.json";

    // TODO: Get rid of Mavenizer eventually
    public static final String MAVENIZER_OUTPUT = "mavenizer/output/net/minecraft/joined/1.21.9/joined-1.21.9.jar";
    public static final String LOADER_VERSION;

    static {
        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("internal/buildinfo.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("buildinfo.properties not found in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load buildinfo.properties", e);
        }

        LOADER_VERSION = properties.getProperty("version");
    }
}
