package org.mangorage.paradise;

import org.mangorage.loader.api.mod.IModContainer;
import org.mangorage.paradise.core.game.Game;
import org.mangorage.paradise.game.GameImpl;

public class GameContainer implements IModContainer<Game> {

    public void init() {
        if (GameImpl.getInstance() == null) GameImpl.main(new String[]{});
    }


    @Override
    public Game getInstance() {
        return GameImpl.getInstance();
    }

    @Override
    public String getId() {
        return "game";
    }
}
