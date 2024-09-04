package net.naxxsoftwares.mod.modules;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.MinecraftClient;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

public class Modules implements Iterable<Module> {

    private static final List<Module> modules = new ArrayList<>();
    private static final Map<Class<? extends Module>, Module> moduleMap = new Object2ObjectArrayMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(Modules.class);
    private static final MinecraftClient client = Initializer.client;

    // Adds a module to the list
    public static <T extends Module> void addModule(@NotNull T module) {
        modules.add(module);
        moduleMap.put(module.getClass(), module);
    }

    public static boolean isModuleValid(@NotNull Class<? extends Module> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers()))
            return false;
        if (getModuleByClass(clazz) != null)
            return false;
        return modules.stream().noneMatch(module1 -> Objects.equals(Module.getStringName(clazz), Module.getStringName(module1)));
    }

    public static @Nullable Module getModuleByClass(Class<? extends Module> module) {
        return moduleMap.get(module);
    }

    // Retrieves all modules as an unmodifiable list
    @Contract(pure = true)
    public static @Unmodifiable List<? extends Module> getAllModules() {
        return List.copyOf(modules);
    }

    // Checks if the module is active
    public static boolean isModuleActive(Class<? extends Module> clazz) {
        Module module = getModuleByClass(clazz);
        return module != null && module.isActive();
    }

    public static void run(@NotNull Module module) {
        if (module.isActive() && client.player != null)
            module.run();
    }

    // Allows iteration over the modules
    @Override
    public @NotNull Iterator<Module> iterator() {
        return new ModuleIterator();
    }

    // Internal iterator class for iterating over the modules
    private static class ModuleIterator implements Iterator<Module> {
        private int index = 0;

        @Contract(pure = true)
        @Override
        public boolean hasNext() {
            return index < modules.size();
        }

        @Override
        public Module next() {
            return modules.get(index++);
        }
    }
}
