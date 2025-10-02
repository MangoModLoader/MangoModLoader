package org.mangorage.boot.loader.api;


import org.mangorage.boot.loader.JPMSGameClassloader;

public sealed interface IMangoLoader permits JPMSGameClassloader {
    byte[] getClassBytes(String name);
    boolean hasClass(String name);
}