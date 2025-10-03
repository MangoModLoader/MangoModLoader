package org.mangorage.paradise;

import org.mangorage.loader.api.mod.IModContainer;
import org.mangorage.paradise.core.game.Game;
import org.mangorage.paradise.game.GameImpl;

public class GameContainer implements IModContainer<Game> {

    @Override
    public Game getInstance() {
        if (GameImpl.getInstance() == null) GameImpl.main(new String[]{});
        return GameImpl.getInstance();
    }

    @Override
    public String getId() {
        return "game";
    }
}
