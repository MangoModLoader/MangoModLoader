package org.mangorage.mixinsupport.plugin;

import org.mangorage.loader.api.mod.IModContainer;
import org.spongepowered.asm.mixin.Mixins;

public final class MangoBotMixinPlugin implements IModContainer<Object> {
    public static final String ID = "mangobotmixin";

    public void init() {
    }

    @Override
    public Object getInstance() {
        return null;
    }

    @Override
    public String getId() {
        return ID;
    }
}
