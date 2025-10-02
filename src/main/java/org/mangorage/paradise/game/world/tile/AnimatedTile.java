package org.mangorage.paradise.game.world.tile;

import org.mangorage.paradise.core.util.Pos;
import org.mangorage.paradise.core.util.Size;
import org.mangorage.paradise.core.game.tile.Tile;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class AnimatedTile implements Tile {

    private final Pos pos;
    private final Size size;
    private final Image image;

    public AnimatedTile(Pos pos, Size size, BufferedImage image) {
        this.pos = pos;
        this.size = size;

        this.image = image.getScaledInstance(getSize().width(), getSize().height(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public void update() {
        // handle animation logic here if you have multiple frames
    }

    @Override
    public void render(Graphics graphics) {
        graphics.drawImage(image, 0, 0, null);
    }
}