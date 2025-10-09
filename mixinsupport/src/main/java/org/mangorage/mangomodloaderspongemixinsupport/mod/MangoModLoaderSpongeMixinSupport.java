package org.mangorage.mangomodloaderspongemixinsupport.mod;

import org.mangorage.loader.api.LoaderConstants;
import org.mangorage.loader.api.mod.IModContainer;

public final class MangoModLoaderSpongeMixinSupport implements IModContainer<Object> {
    public static final String ID = "mangomodloaderspongemixinsupport";

    @Override
    public Object getInstance() {
        return null;
    }

    @Override
    public String getVersion() {
        return LoaderConstants.getLoaderVersion();
    }

    @Override
    public String getId() {
        return ID;
    }
}
