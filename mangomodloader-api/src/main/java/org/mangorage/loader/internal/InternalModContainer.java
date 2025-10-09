package org.mangorage.loader.internal;

import org.mangorage.loader.api.mod.IModContainer;

public final class InternalModContainer implements IModContainer<Object> {
    public static final InternalModContainer INSTANCE = new InternalModContainer();

    @Override
    public Object getInstance() {
        return null;
    }

    @Override
    public String getVersion() {
        return Constants.LOADER_VERSION;
    }

    @Override
    public String getId() {
        return Constants.MOD_ID;
    }
}
