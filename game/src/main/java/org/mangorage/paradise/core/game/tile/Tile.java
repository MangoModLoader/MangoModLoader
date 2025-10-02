package org.mangorage.paradise.core.game.tile;

import org.mangorage.paradise.core.util.Pos;
import org.mangorage.paradise.core.util.Size;

import java.awt.*;

public interface Tile {
    Pos getPos();
    Size getSize();

    void update();
    void render(Graphics graphics);
}
