package org.mangorage.loader.api;

import org.mangorage.loader.api.mod.Environment;
import org.mangorage.loader.internal.Constants;

/**
 * For public API use...
 */
public final class LoaderConstants {
    public static String getMCVersion() {
        return Constants.MINECRAFT_VERSION;
    }

    public static String getLoaderVersion() {
        return Constants.LOADER_VERSION;
    }

    public static String getLoaderModId() {
        return Constants.MOD_ID;
    }

    public static Environment getSide() {
        return Environment.CLIENT;
    }
}
