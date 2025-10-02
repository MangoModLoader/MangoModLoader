package org.mangorage.paradise.game.world;

import org.mangorage.paradise.core.game.Game;
import org.mangorage.paradise.core.keybind.DefaultKeyBind;
import org.mangorage.paradise.core.keybind.KeyBindManager;
import org.mangorage.paradise.core.util.Pos;
import org.mangorage.paradise.core.util.Size;
import org.mangorage.paradise.game.world.tile.AnimatedTile;
import org.mangorage.paradise.game.world.tile.CharacterTile;
import org.mangorage.paradise.game.world.tile.TestTile;
import org.mangorage.paradise.core.game.GameCanvas;
import org.mangorage.paradise.core.keybind.KeyEventType;
import org.mangorage.paradise.core.game.tile.Tile;

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
    private double zoom = 2.0; // 1.0 = normal, >1 = zoom in, <1 = zoom out

    private final KeyBindManager keybindManager = new KeyBindManager();
    private final List<Tile> tiles = new CopyOnWriteArrayList<>();

    public World() {
        // Zoom controls
        keybindManager.registerKeyBind("zoomIn", new DefaultKeyBind(KeyEvent.VK_EQUALS, false));
        keybindManager.registerKeyBind("zoomOut", new DefaultKeyBind(KeyEvent.VK_MINUS, false));
        keybindManager.registerKeyBind("resetZoom", new DefaultKeyBind(KeyEvent.VK_0, false));

        // Debug + camera control
        keybindManager.registerKeyBind("debug", new DefaultKeyBind(KeyEvent.VK_F3, true));
        keybindManager.registerKeyBind("updateCamera", new DefaultKeyBind(KeyEvent.VK_SHIFT, false));

        // Movement
        keybindManager.registerKeyBind("left", new DefaultKeyBind(KeyEvent.VK_A, false));
        keybindManager.registerKeyBind("right", new DefaultKeyBind(KeyEvent.VK_D, false));
        keybindManager.registerKeyBind("up", new DefaultKeyBind(KeyEvent.VK_W, false));
        keybindManager.registerKeyBind("down", new DefaultKeyBind(KeyEvent.VK_S, false));

        // test tiles
        tiles.add(new TestTile(Pos.of(1, 1)));
        tiles.add(new AnimatedTile(Pos.of(1, 10), Size.of(64, 64),
                Game.getInstance().getAssetManager().getInternalImage("assets/textures/example.png")));

        tiles.add(new CharacterTile(Pos.of(10, 10)));
        for (int i = 0; i < 1000 ; i++) {
            tiles.add(
                    new CharacterTile(
                            Pos.of(10 + i, 10)
                    )
            );
        }
    }

    public void render(GameCanvas canvas, Graphics2D g, int frames, int updates) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Apply zoom + camera translate
        g2.translate(canvas.getWidth() / 2.0, canvas.getHeight() / 2.0);
        g2.scale(zoom, zoom);
        g2.translate(-camX, -camY);

        int tileSize = 32;

        // ==== GRID ====
        if (keybindManager.isActive("debug")) {
            g2.setColor(Color.DARK_GRAY);

            int worldLeft   = (int) ((camX - canvas.getWidth()  / 2.0 / zoom) / tileSize) - 1;
            int worldRight  = (int) ((camX + canvas.getWidth()  / 2.0 / zoom) / tileSize) + 1;
            int worldTop    = (int) ((camY - canvas.getHeight() / 2.0 / zoom) / tileSize) - 1;
            int worldBottom = (int) ((camY + canvas.getHeight() / 2.0 / zoom) / tileSize) + 1;

            for (int x = worldLeft; x <= worldRight; x++) {
                g2.drawLine(x * tileSize, worldTop * tileSize, x * tileSize, worldBottom * tileSize);
            }
            for (int y = worldTop; y <= worldBottom; y++) {
                g2.drawLine(worldLeft * tileSize, y * tileSize, worldRight * tileSize, y * tileSize);
            }
        }

        // ==== RENDER TILES ====
        int worldLeft   = (int) ((camX - canvas.getWidth()  / 2.0 / zoom) / tileSize) - 1;
        int worldRight  = (int) ((camX + canvas.getWidth()  / 2.0 / zoom) / tileSize) + 1;
        int worldTop    = (int) ((camY - canvas.getHeight() / 2.0 / zoom) / tileSize) - 1;
        int worldBottom = (int) ((camY + canvas.getHeight() / 2.0 / zoom) / tileSize) + 1;

        for (Tile tile : tiles) {
            int tx = tile.getPos().x();
            int ty = tile.getPos().y();

            // skip tiles outside visible area
            if (tx < worldLeft || tx > worldRight || ty < worldTop || ty > worldBottom) continue;

            int screenX = tx * tileSize;
            int screenY = ty * tileSize;
            int width   = tile.getSize().width();
            int height  = tile.getSize().height();

            tile.render(g2.create(screenX, screenY, width, height));
        }

        // ==== PLAYER ====
        g2.setColor(Color.RED);
        g2.fillRect(playerX * tileSize, playerY * tileSize, tileSize, tileSize);

        g2.dispose();

        // ==== UI OVERLAY (not zoomed) ====
        g.setColor(Color.WHITE);
        g.drawString("Player Tile: (" + playerX + ", " + playerY + ")", 10, 20);
        g.drawString("Camera: (" + camX + ", " + camY + ")", 10, 40);
        g.drawString("Shift Held: " + shiftHeld, 10, 60);
        g.drawString("Zoom: " + zoom, 10, 80);
        g.drawString("UPS: " + updates + ", FPS: " + frames, 10, 100);
    }

    public void update() {
        tiles.forEach(Tile::update);

        // Zoom controls
        if (keybindManager.isActive("zoomIn")) zoom +=  0.05;
        if (keybindManager.isActive("zoomOut")) zoom -= 0.05;
        if (keybindManager.isActive("resetZoom")) zoom = 1.0;

        if (zoom < 0.2)
            zoom = 0.2;

        // Camera logic
        shiftHeld = keybindManager.isActive("updateCamera");
        if (shiftHeld) {
            camX = playerX * 32;
            camY = playerY * 32;
        }

        // Movement
        if (keybindManager.isActive("up")) playerY--;
        if (keybindManager.isActive("down")) playerY++;
        if (keybindManager.isActive("left")) playerX--;
        if (keybindManager.isActive("right")) playerX++;
    }

    public void onKeyEvent(KeyEvent event, KeyEventType type) {
        if (type == KeyEventType.PRESSED) {
            keybindManager.onPress(event.getKeyCode());
        } else if (type == KeyEventType.RELEASED) {
            keybindManager.onRelease(event.getKeyCode());
        }
    }
}

