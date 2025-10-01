package org.mangorage.paradise.core.util;

public record Pos(int x, int y) {
    public static Pos of(int x, int y) {
        return new Pos(x, y);
    }
}
