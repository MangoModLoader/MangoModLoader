package org.mangorage.paradise.core.asset;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class AssetManager {
    private final Map<String, BufferedImage> imageCache = new HashMap<>();

    public Reader readInternalAsset(String path) {
        return new InputStreamReader(getInternalAsset(path));
    }

    public InputStream getInternalAsset(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    public BufferedImage getInternalImage(String path) {
        return imageCache.computeIfAbsent(path, p -> {
            final var is = getInternalAsset(p);
            try {
                return ImageIO.read(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
