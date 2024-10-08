package net.naxxsoftwares.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.naxxsoftwares.mod.events.EventRegisterer;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.modules.Modules;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;

public final class Initializer implements ClientModInitializer {
    public static final ModMetadata MOD_META;
    public static final String MOD_ID = "cheatmod";
    public static final String MOD_NAME;
    public static final String MOD_VERSION;
    public static final MinecraftClient client;
    public static final boolean isDevelopmentEnvironment;
    private static final Logger LOGGER;

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(() -> new NoSuchElementException("Mod container not found for ID: " + MOD_ID)).getMetadata();
        MOD_NAME = MOD_META.getName();
        MOD_VERSION = MOD_META.getVersion().getFriendlyString();
        LOGGER = LoggerFactory.getLogger(String.format("%s / Initializer", MOD_NAME));
        client = MinecraftClient.getInstance();
        isDevelopmentEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Heyyy {} !", client.getGameProfile().getName());

        //Register modules
        Reflections reflections = new Reflections("net.naxxsoftwares.mod.modules");
        List<Class<? extends Module>> modules = reflections.getSubTypesOf(Module.class).stream().toList();

        int skippedModules = 0;
        long timeAtStart = System.currentTimeMillis();

        for (Class<? extends Module> clazz : modules) {
            try {
                if (Modules.isModuleValid(clazz)) {
                    Module module = clazz.getDeclaredConstructor().newInstance();
                    Modules.addModule(module);
                    EventRegisterer.registerEvents(module);
                } else {
                    LOGGER.error("Skipping \"{}\" as it is not a valid module !", clazz.getSimpleName());
                    skippedModules++;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate \"{}\" : {}", clazz.getSimpleName(), e.getMessage());
                skippedModules++;
            }
        }

        long timeAtEnd = System.currentTimeMillis();
        long duration = timeAtEnd - timeAtStart;

        if (skippedModules == 0) LOGGER.info("All modules were successfully instantiated in {} ms !", duration);
        else LOGGER.warn("Modules instantiated in {} ms. {} modules were skipped !", timeAtEnd - timeAtStart, skippedModules);

        Runtime.getRuntime().gc();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.info("That was {} v{} by PurpyNaxx_ !", MOD_NAME, MOD_VERSION)));
    }
}
