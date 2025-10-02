package org.mangorage.paradise.core.loader.api;


import org.mangorage.paradise.core.loader.JPMSGameClassloader;

public sealed interface IMangoLoader permits JPMSGameClassloader {
    byte[] getClassBytes(String name);
    boolean hasClass(String name);
}