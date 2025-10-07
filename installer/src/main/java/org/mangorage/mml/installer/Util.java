package org.mangorage.mml.installer;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Util {
    public static void checkPath(Path path) {
        System.out.println("Found -> " + path.toAbsolutePath() + " Exists -> " + Files.exists(path.toAbsolutePath()));
    }
}
