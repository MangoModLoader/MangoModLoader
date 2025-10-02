module minersparadise {
    requires com.google.gson;
    requires java.desktop;


    exports org.mangorage.paradise.core.asset.sprite;
    exports org.mangorage.paradise.core.asset;
    exports org.mangorage.paradise.core.game;
    exports org.mangorage.paradise.core.game.tile;
    exports org.mangorage.paradise.core.keybind;

    exports org.mangorage.paradise.core.loader.api;
    exports org.mangorage.paradise.core.util;

    exports org.mangorage.paradise.data;
    exports org.mangorage.paradise.game.world.tile;
    exports org.mangorage.paradise.game;

    opens assets.sprites.wizard;
    opens assets.textures;

    uses org.mangorage.paradise.core.loader.api.IClassTransformer;
    uses org.mangorage.paradise.core.loader.api.IModuleConfigurator;
}