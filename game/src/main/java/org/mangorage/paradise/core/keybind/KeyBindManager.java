package org.mangorage.paradise.core.keybind;

import java.util.HashMap;
import java.util.Map;

public final class KeyBindManager {
    private final Map<String, KeyBind> keyBindMap = new HashMap<>();

    public void onPress(int key) {
        keyBindMap.forEach((id, b) -> b.onPress(key));
    }

    public void onRelease(int key) {
        keyBindMap.forEach((id, b) -> b.onRelease(key));
    }

    public void registerKeyBind(String id, KeyBind keyBind) {
        keyBindMap.put(id, keyBind);
    }

    public boolean isActive(String id) {
        final var bind = keyBindMap.get(id);
        if (bind == null) return false;
        return bind.isActive();
    }
}
