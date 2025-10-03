package org.mangorage.loader;

import org.mangorage.loader.api.mod.IModContainer;

import java.util.Comparator;
import java.util.ServiceLoader;

public final class ModLoader {
    public static void loadMods(ModuleLayer layer) {
        System.out.println("Starting to load mod containers!");

        final var mods = ServiceLoader.load(layer, IModContainer.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .sorted(
                        Comparator.comparing(
                                mod -> !mod.getId().equals("game")
                        )
                )
                .toList();


        mods.forEach(mod -> {
            System.out.println("Loading mod -> " + mod.getId());
            mod.getInstance();
        });

    }
}
