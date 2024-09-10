package net.naxxsoftwares.mod.modules;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Predicate;

public class Modules implements Iterable<Module> {

    private static final List<Module> modules = new ArrayList<>();
    private static final Map<Class<? extends Module>, Module> moduleMap = new Object2ObjectArrayMap<>();

    // Adds a module to the list
    public static <T extends Module> void addModule(@NotNull T module) {
        modules.add(module);
        moduleMap.put(module.getClass(), module);
    }

    public static boolean isModuleValid(@NotNull Class<? extends Module> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) return false;
        if (getModuleByClass(clazz) != null) return false;
        return modules.stream().noneMatch(module -> Objects.equals(Module.getStringName(clazz), Module.getStringName(module)));
    }

    public static @Nullable Module getModuleByClass(Class<? extends Module> module) {
        return moduleMap.get(module);
    }

    // Retrieves all modules as an unmodifiable list
    public static @Unmodifiable List<? extends Module> getAllModules() {
        return List.copyOf(modules);
    }

    // Retrieves all modules matching predicate as an unmodifiable list
    public static @Unmodifiable List<? extends Module> getAllModulesMatching(Predicate<? super Module> predicate) {
        return List.copyOf(modules.stream().filter(predicate).toList());
    }

    // Checks if the module is active
    public static boolean isModuleActive(Class<? extends Module> clazz) {
        Module module = getModuleByClass(clazz);
        return module != null && module.isActive();
    }

    // Allows iteration over the modules
    @Override
    public @NotNull Iterator<Module> iterator() {
        return new ModuleIterator();
    }

    // Internal iterator class for iterating over the modules
    private static class ModuleIterator implements Iterator<Module> {
        private int index = 0;

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
