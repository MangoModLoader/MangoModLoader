package org.mangorage.paradise.core.game;

import org.mangorage.paradise.GameImpl;
import org.mangorage.paradise.core.asset.AssetManager;
import org.mangorage.paradise.core.asset.SpriteManager;
import org.mangorage.paradise.core.keybind.KeyBindManager;

public interface Game {
    static Game getInstance() {
        return GameImpl.getInstance();
    }


    AssetManager getAssetManager();
    KeyBindManager getKeyBindManager();
    SpriteManager getSpriteManager();
}
