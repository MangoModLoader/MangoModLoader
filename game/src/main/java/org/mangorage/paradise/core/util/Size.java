package org.mangorage.paradise.core.util;

public record Size(int width, int height) {
    public static Size of(int width, int height) {
        return new Size(width, height);
    }
}
