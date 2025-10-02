package org.mangorage.paradise.core.keybind;

public interface KeyBind {
    boolean isActive();

    void onPress(int key);
    void onRelease(int key);
}
