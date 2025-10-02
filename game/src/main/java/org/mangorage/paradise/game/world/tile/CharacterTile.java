package org.mangorage.paradise.game.world.tile;

import org.mangorage.paradise.core.game.Game;
import org.mangorage.paradise.core.game.tile.Tile;
import org.mangorage.paradise.core.util.Pos;
import org.mangorage.paradise.core.util.Size;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class CharacterTile implements Tile {
    private final Pos pos;

    public CharacterTile(Pos pos) {
        this.pos = pos;
    }

    @Override
    public Pos getPos() {
        return pos;
    }

    @Override
    public Size getSize() {
        return Size.of(32, 64);
    }

    private int ticks = 0;
    private int frame = 0;

    @Override
    public void update() {
        ticks++;

            frame++;
            if (frame >= 6)
                frame = 1;

    }

    @Override
    public void render(Graphics g) {
        // 96 pixels apart
        g.drawImage(
                Game.getInstance().getSpriteManager().getSprite("furnace", frame)
                        .getScaledInstance(getSize().width(), getSize().height(), BufferedImage.TYPE_INT_ARGB),
                0, 0, null
        );
    }
}
