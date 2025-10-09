package org.mangorage.loader.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Constants {
    public static final String MINECRAFT_VERSION = "1.21.9";
    public static final String MINECRAFT_VERSIONS_JSON_URL = "https://piston-meta.mojang.com/v1/packages/d7a33415a8e68a8fdff87ab2020e64de021df302/1.21.9.json";
    public static final String MOD_ID = "mangomodloader";

    public static final String LOADER_VERSION;
    static {
        Properties properties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/mangomodloader/buildinfo.properties")) {
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
