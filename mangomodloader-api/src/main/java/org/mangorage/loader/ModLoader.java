package org.mangorage.loader;

import org.mangorage.loader.api.mod.IModContainer;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Comparator;
import java.util.ServiceLoader;

public final class ModLoader {


    /**
     * Returns the current JVM uptime as a Duration.
     */
    public static Duration getUptime() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Duration.ofMillis(runtimeMXBean.getUptime());
    }

    /**
     * Returns a human-readable string of the JVM uptime, e.g., "1d 2h 3m 4s".
     */
    public static String getUptimeString() {
        Duration uptime = getUptime();
        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    public static void loadMods(ModuleLayer layer) {
        System.out.println("Starting to load mod containers!");

        final var mods = ServiceLoader.load(layer, IModContainer.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .sorted(
                        Comparator.comparing(
                                mod -> !mod.getId().equals("game")
                        )
                )
                .toList();


        mods.forEach(mod -> {
            System.out.println("Loading mod -> " + mod.getId());
            callInit(mod);
        });

        System.out.println("Took " + getUptime().toMillis() + "ms to load everything!");
    }

    public static void callInit(IModContainer<?> container) {
        try {
            Method initMethod = container.getClass().getMethod("init");
            initMethod.invoke(container);
        } catch (Throwable e) {
            throw new RuntimeException("Couldn't reflectively call init because something exploded.", e);
        }
    }
}
