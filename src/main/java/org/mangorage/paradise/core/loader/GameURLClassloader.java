package org.mangorage.paradise.core.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class GameURLClassloader extends URLClassLoader {
    public GameURLClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
