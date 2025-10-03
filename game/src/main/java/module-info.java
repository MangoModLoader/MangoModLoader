module minersparadise {
    requires loader;
    requires java.desktop;
    requires com.google.gson;


    exports org.mangorage.paradise;
    opens org.mangorage.paradise to loader;

    exports org.mangorage.paradise.core.asset.sprite;
    exports org.mangorage.paradise.core.asset;
    exports org.mangorage.paradise.core.game;
    exports org.mangorage.paradise.core.game.tile;
    exports org.mangorage.paradise.core.keybind;

    exports org.mangorage.paradise.core.util;

    exports org.mangorage.paradise.data;
    exports org.mangorage.paradise.game.world.tile;
    exports org.mangorage.paradise.game;

    opens assets.sprites.wizard;
    opens assets.sprites;

    opens assets.textures;
    opens assets.textures.furnace;

    uses org.mangorage.loader.api.mod.IModContainer;
    provides org.mangorage.loader.api.mod.IModContainer with org.mangorage.paradise.GameContainer;
}