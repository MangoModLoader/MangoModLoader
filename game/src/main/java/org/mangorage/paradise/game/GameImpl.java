package org.mangorage.paradise.game;

import org.mangorage.paradise.core.DataLoader;
import org.mangorage.paradise.core.asset.AssetManager;
import org.mangorage.paradise.core.asset.SpriteManager;
import org.mangorage.paradise.core.game.Game;
import org.mangorage.paradise.core.game.GameCanvas;
import org.mangorage.paradise.core.keybind.KeyBindManager;
import org.mangorage.paradise.core.keybind.KeyEventType;
import org.mangorage.paradise.game.world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;

public final class GameImpl extends Canvas implements Game, GameCanvas {

    private static Game game;

    public static Game getInstance() {
        return game;
    }

    private boolean running;
    private Thread gameThread;

    private final int TARGET_UPS = 30;
    private final int TARGET_FPS = 10_000;

    private AssetManager assetManager;
    private KeyBindManager keyBindManager;
    private SpriteManager spriteManager;

    private World world;

    GameImpl() {
        JFrame frame = new JFrame("Grid Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                world.onKeyEvent(e, KeyEventType.TYPED);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                world.onKeyEvent(e, KeyEventType.PRESSED);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                world.onKeyEvent(e, KeyEventType.RELEASED);
            }
        });

        // Triple buffering for smoother graphics
        this.createBufferStrategy(3);
    }

    void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this::run, "GameLoop");
        gameThread.start();
    }

    void init() {
        assetManager = new AssetManager();
        keyBindManager = new KeyBindManager();
        spriteManager = new SpriteManager();
        world = new World();
    }

    public void stop() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void render(int frames, int updates) {
        BufferStrategy bs = getBufferStrategy();
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Clear screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Render world
        world.render(this, g, frames, updates);

        g.dispose();
        bs.show();
    }

    @Override
    public void update(Graphics g) {}

    void run() {
        final double UPDATE_INTERVAL = 1_000_000_000.0 / TARGET_UPS;
        final double FRAME_INTERVAL  = 1_000_000_000.0 / TARGET_FPS;

        long lastUpdate = System.nanoTime();
        long lastRender = System.nanoTime();

        int frames = 0;
        int updates = 0;

        int lastFrames = 0;
        int lastUpdates = 0;

        long timer = System.currentTimeMillis();

        while (running) {
            long now = System.nanoTime();

            // Update game logic
            while (now - lastUpdate >= UPDATE_INTERVAL) {
                world.update();
                updates++;
                lastUpdate += UPDATE_INTERVAL;
            }

            // Render frame
            if (now - lastRender >= FRAME_INTERVAL) {
                render(lastFrames, lastUpdates);
                frames++;
                lastRender += FRAME_INTERVAL;
            }

            // Print FPS and UPS every second
            if (System.currentTimeMillis() - timer >= 1000) {
                lastFrames = frames;
                lastUpdates = updates;

                updates = 0;
                frames = 0;
                timer += 1000;
            }
        }
    }

    @Override
    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public KeyBindManager getKeyBindManager() {
        return keyBindManager;
    }

    @Override
    public SpriteManager getSpriteManager() {
        return spriteManager;
    }


    public static void main(String[] args) {
        GameImpl instance = new GameImpl();
        game = instance;
        instance.init();
        DataLoader.initSpritesData();
        instance.start();
    }
}
