package org.mangorage.paradise;

import org.mangorage.paradise.core.Tile;

import java.awt.*;
import java.util.Random;

public class TestTile implements Tile {
    private static final Color[] colors = new Color[]{Color.RED, Color.BLUE, Color.ORANGE};
    private static final Random random = new Random();

    private final int x;
    private final int y;

    private int ticks = 0;
    private Color selectedColor = colors[0];


    public TestTile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 64;
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