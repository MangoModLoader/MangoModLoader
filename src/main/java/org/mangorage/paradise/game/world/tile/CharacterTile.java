package org.mangorage.paradise.game.world.tile;

import org.mangorage.paradise.core.game.Game;
import org.mangorage.paradise.core.game.tile.Tile;
import org.mangorage.paradise.core.util.Pos;
import org.mangorage.paradise.core.util.Size;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class CharacterTile implements Tile {

    private final BufferedImage image = Game.getInstance().getAssetManager().getInternalImage("assets/textures/idle.png");

    @Override
    public Pos getPos() {
        return Pos.of(50, 10);
    }

    @Override
    public Size getSize() {
        return Size.of(32 * 2, 32 * 4);
    }

    private int ticks = 0;

    private int frame = 0;

    @Override
    public void update() {
        ticks++;
        if (ticks % 40 == 0) {
            frame++;
            if (frame >= 9)
                frame = 0;
        }
    }

    @Override
    public void render(Graphics g) {
        // 96 pixels apart
        g.drawImage(
                Game.getInstance().getSpriteManager().getSprite("idle", frame)
                        .getScaledInstance(getSize().width(), getSize().height(), BufferedImage.TYPE_INT_ARGB),
                0, 0, null
        );
    }
}
