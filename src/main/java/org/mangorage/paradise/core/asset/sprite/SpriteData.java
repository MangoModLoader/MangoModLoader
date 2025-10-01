package org.mangorage.paradise.core.asset.sprite;

import java.util.List;

public record SpriteData(
        String id,
        long msPerFrame,
        List<FrameData> frameData
) {
    public int getFrameCount() {
        return frameData.size();
    }
}
