package org.mangorage.paradise.core;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public final class KeybindManager {

    private final Map<Integer, State> stateMap = new HashMap<>();
    private final Map<Integer, Boolean> toggleMap = new HashMap<>();

    public void onPress(int key) {
        if (getToggledState(key)) {
            var active = isActive(key);
            stateMap.put(key, active ? State.INACTIVE : State.ACTIVE);
        } else {
            stateMap.put(key, State.ACTIVE);
        }
    }

    public void onRelease(int key) {
        if (!getToggledState(key)) {
            stateMap.put(key, State.INACTIVE);
        }
    }

    public boolean isActive(int key) {
        return stateMap.containsKey(key) && stateMap.get(key) == State.ACTIVE;
    }

    public void setToggleState(int key, boolean state) {
        toggleMap.put(key, state);
    }

    public boolean getToggledState(int key) {
        return toggleMap.getOrDefault(key, false);
    }

    public enum State {
        ACTIVE,
        INACTIVE
    }
}
