package org.mangorage.paradise.world.tile;

import org.mangorage.paradise.core.game.tile.Tile;
import org.mangorage.paradise.core.util.Pos;
import org.mangorage.paradise.core.util.Size;

import java.awt.*;
import java.util.Random;

public class TestTile implements Tile {
    private static final Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.ORANGE};
    private static final Random random = new Random();


    private final Size size = new Size(32, 32);
    private final Pos pos;


    private int ticks = 0;
    private Color selectedColor = colors[0];


    public TestTile(Pos pos) {
        this.pos = pos;
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
        // static tile, nothing to update for now
        ticks++;
        if (ticks % 40 == 0) {
            selectedColor = colors[random.nextInt(colors.length)];
        }
    }

    @Override
    public void render(Graphics g) {
        g.setColor(selectedColor);
        g.fillRect(0, 0, 64, 64); // render relative to provided screen coordinates
    }
}