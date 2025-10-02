package org.mangorage.paradise.core.asset;

import com.google.gson.Gson;
import org.mangorage.paradise.core.asset.sprite.FrameData;
import org.mangorage.paradise.core.asset.sprite.SpriteData;
import org.mangorage.paradise.core.asset.sprite.SpriteInstance;
import org.mangorage.paradise.core.game.Game;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SpriteManager {
    private static final Gson GSON = new Gson();

    private final Map<String, SpriteInstance> cache = new HashMap<>();

    public void loadJson(String path) {
        SpriteData spriteData = GSON.fromJson(
                Game.getInstance().getAssetManager().readInternalAsset(path), SpriteData.class
        );

        List<FrameData> frameData = spriteData.frameData();

        final var frames = frameData
                .stream()
                .map(data -> Game.getInstance().getAssetManager().getInternalImage(data.texture())
                        .getSubimage(
                                data.position().x(), data.position().y(),
                                data.width(), data.height()
                        )).toList();

        cache.put(spriteData.id(), new SpriteInstance(frames, spriteData.msPerFrame()));
    }

    public Image getSprite(String id, int frame) {
        return cache.get(id).getFrame(frame);
    }
}
