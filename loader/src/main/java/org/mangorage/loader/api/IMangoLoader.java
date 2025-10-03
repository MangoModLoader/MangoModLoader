package org.mangorage.loader.api;


import org.mangorage.loader.internal.JPMSGameClassloader;

public sealed interface IMangoLoader permits JPMSGameClassloader {
    byte[] getClassBytes(String name);
    boolean hasClass(String name);
}