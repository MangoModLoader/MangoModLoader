package org.mangorage.paradise.core.keybind;

public final class DefaultKeyBind implements KeyBind {

    private final int keyCode;
    private final boolean toggle;

    private boolean active = false;

    public DefaultKeyBind(int keyCode, boolean toggle) {
        this.keyCode = keyCode;
        this.toggle = toggle;
    }


    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void onPress(int key) {
        if (key == keyCode) {
            active = !toggle || !active;
        }
    }

    @Override
    public void onRelease(int key) {
        if (toggle) return;
        if (key == keyCode) {
            active = false;
        }
    }
}
