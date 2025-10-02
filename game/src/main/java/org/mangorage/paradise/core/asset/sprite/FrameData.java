package org.mangorage.paradise.core.asset.sprite;

import org.mangorage.paradise.core.util.Pos;

public record FrameData(
        String texture,
        Pos position,
        int width, int height
) {}
