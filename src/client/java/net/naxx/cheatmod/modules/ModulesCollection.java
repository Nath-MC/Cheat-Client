package net.naxx.cheatmod.modules;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

public class ModulesCollection implements Iterable<Module> {

    private static final ArrayList<Module> modules = new ArrayList<>();

    public static <T extends Module> void addModule(T module) {
        modules.add(module);
    }

    @Override
    public @NotNull Iterator<Module> iterator() {
        return new ModuleIterator();
    }

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
