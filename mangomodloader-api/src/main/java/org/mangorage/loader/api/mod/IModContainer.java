package org.mangorage.loader.api.mod;

import java.util.List;

public interface IModContainer<O> {
    O getInstance();

    default List<String> getRequiredDependencies() {
        return List.of();
    }

    String getVersion();
    String getId();
}
