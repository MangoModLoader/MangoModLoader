package org.mangorage.mangomodloaderspongemixinsupport.mixin.services;

import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MangoModLoaderMixinLoggerImpl extends LoggerAdapterAbstract {
    private static final Map<String, ILogger> LOGGER_MAP = new ConcurrentHashMap<>();

    public static ILogger get(String name) {
        return LOGGER_MAP.computeIfAbsent(name, MangoModLoaderMixinLoggerImpl::new);
    }

    protected MangoModLoaderMixinLoggerImpl(String id) {
        super(id);
    }

    @Override
    public String getType() {
        return "MangoModLoader Mixin Logger";
    }

    @Override
    public void catching(Level level, Throwable throwable) {
        throwable.printStackTrace();
    }

    static void print(String message, Object... args) {
        for (Object arg : args) {
            message = message.replaceFirst("\\{}", arg == null ? "null" : arg.toString());
        }
        System.out.println(message);
    }

    @Override
    public void log(Level level, String s, Object... objects) {
        print(s, objects);
    }

    @Override
    public void log(Level level, String s, Throwable throwable) {
        System.out.println(s);
    }

    @Override
    public <T extends Throwable> T throwing(T t) {
        throw new IllegalStateException(t);
    }
}

