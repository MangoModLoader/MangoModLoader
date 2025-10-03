package org.mangorage.loader.api.mod;

public interface IModContainer<O> {
    O getInstance();

    String getId();
}
