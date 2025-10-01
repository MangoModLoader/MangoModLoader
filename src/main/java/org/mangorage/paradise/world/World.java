package org.mangorage.paradise;

import org.mangorage.paradise.core.GameCanvas;
import org.mangorage.paradise.core.KeyEventType;
import org.mangorage.paradise.core.Tile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class World {
    private int playerX = 1;
    private int playerY = 1;

    private int camX = 0;
    private int camY = 0;

    private boolean shiftHeld = true;

    private final List<Tile> tiles = new CopyOnWriteArrayList<>();

    public World() {
        // add a test tile at (1,1)
        tiles.add(new TestTile(1, 1));
        tiles.add(new TestTile(1, 100));
        tiles.add(new TestTile(1, 200));
    }

    public void render(GameCanvas canvas, Graphics2D g) {
        // draw grid
        g.setColor(Color.DARK_GRAY);
        for (int x = -32; x < canvas.getWidth() + 32; x += 32) {
            for (int y = -32; y < canvas.getHeight() + 32; y += 32) {
                g.drawRect(x - camX % 32, y - camY % 32, 32, 32);
            }
        }

        // render all tiles
        for (Tile tile : tiles) {
            int screenX = (tile.getX() * 32) - camX;
            int screenY = (tile.getY() * 32) - camY;
            int width = tile.getWidth();
            int height = tile.getHeight();
            tile.render(g.create(screenX, screenY, width, height));
        }

        // draw player (at world position adjusted by camera)
        g.setColor(Color.RED);
        int playerScreenX = (playerX * 32) - camX;
        int playerScreenY = (playerY * 32) - camY;
        g.fillRect(playerScreenX, playerScreenY, 32, 32);

        // debug text
        g.setColor(Color.WHITE);
        g.drawString("Player Tile: (" + playerX + ", " + playerY + ")", 10, 20);
        g.drawString("Camera: (" + camX + ", " + camY + ")", 10, 40);
        g.drawString("Shift Held: " + shiftHeld, 10, 60);
    }

    public void update() {
        tiles.forEach(Tile::update);
        if (shiftHeld) {
            // follow the player only when shift is held
            camX = playerX * 32 - 400 + 16; // assuming 800px width
            camY = playerY * 32 - 300 + 16; // assuming 600px height
        }
    }

    public void onKeyEvent(KeyEvent event, KeyEventType type) {
        if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
            if (type == KeyEventType.PRESSED) shiftHeld = true;
            if (type == KeyEventType.RELEASED) shiftHeld = false;
            return;
        }

        if (type == KeyEventType.RELEASED) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_W -> playerY--;
                case KeyEvent.VK_S -> playerY++;
                case KeyEvent.VK_A -> playerX--;
                case KeyEvent.VK_D -> playerX++;
            }
        }
    }
}

