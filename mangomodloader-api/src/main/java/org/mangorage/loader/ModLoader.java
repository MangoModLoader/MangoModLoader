package org.mangorage.loader;

import org.mangorage.loader.api.mod.IModContainer;
import org.mangorage.loader.internal.InternalModContainer;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

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
                .collect(Collectors.toCollection(ArrayList::new));

        mods.add(InternalModContainer.INSTANCE);

        // Sort mods so dependencies load first
        @SuppressWarnings("unchecked")
        List<IModContainer<?>> sortedMods = sortModsByDependencies((List<IModContainer<?>>) (Object) mods);

        System.out.println("Loading mods in dependency order:");
        for (var mod : sortedMods) {
            System.out.println("- " + mod.getId() + " (depends on " + mod.getRequiredDependencies() + ")");
            callInit(mod);
        }

        System.out.println("Took " + getUptime().toMillis() + "ms to load everything!");
    }

    private static List<IModContainer<?>> sortModsByDependencies(List<IModContainer<?>> mods) {
        List<IModContainer<?>> sorted = new ArrayList<>();
        List<IModContainer<?>> visited = new ArrayList<>();

        // Quick lookup by mod ID
        Map<String, IModContainer<?>> modMap = mods.stream().collect(Collectors.toMap(IModContainer::getId, m -> m));

        for (var mod : mods) {
            visit(mod, modMap, visited, sorted, new ArrayList<>());
        }

        return sorted;
    }

    private static void visit(IModContainer<?> mod, Map<String, IModContainer<?>> modMap, List<IModContainer<?>> visited, List<IModContainer<?>> sorted, List<String> stack) {
        if (visited.contains(mod))
            return;

        if (stack.contains(mod.getId()))
            throw new IllegalStateException("Circular dependency detected: " + String.join(" -> ", stack) + " -> " + mod.getId());

        stack.add(mod.getId());

        for (var depId : mod.getRequiredDependencies()) {
            var dep = modMap.get(depId);
            if (dep == null)
                throw new IllegalStateException("Missing dependency '" + depId + "' for mod " + mod.getId());
            visit(dep, modMap, visited, sorted, stack);
        }

        visited.add(mod);
        sorted.add(mod);
        stack.remove(mod.getId());
    }

    public static void callInit(IModContainer<?> container) {
        try {
            Method initMethod = container.getClass().getMethod("init");
            initMethod.invoke(container);
        } catch (NoSuchMethodException e) {
            System.out.println("Could not find init method for " + "ModContainer[%s, %s]".formatted(container.getClass(), container.getId()));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
