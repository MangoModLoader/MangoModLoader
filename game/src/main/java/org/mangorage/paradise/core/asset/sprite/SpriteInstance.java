package org.mangorage.paradise.core.asset.sprite;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class SpriteInstance {
    private final List<? extends Image> frames;
    private long msPerFrame;

    public SpriteInstance(List<? extends Image> frames, long msPerFrame) {
        this.frames = frames;
        this.msPerFrame = msPerFrame;
    }

    public Image getFrame(int frame) {
        return frames.get(frame);
    }

    public SpriteInstance getScaledInstance(Function<Image, Image> function) {
        final List<Image> newFrames = new ArrayList<>();
        frames.forEach(image -> {
            newFrames.add(function.apply(image));
        });

        return new SpriteInstance(newFrames, msPerFrame);
    }
}
