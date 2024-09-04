package net.naxxsoftwares.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
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
    public static final MinecraftClient client;
    private static final Logger LOGGER;

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(() -> new NoSuchElementException("Mod container not found for ID: " + MOD_ID)).getMetadata();
        MOD_NAME = MOD_META.getName();
        LOGGER = LoggerFactory.getLogger(String.format("%s / Initializer", MOD_NAME));
        client = MinecraftClient.getInstance();
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

                    if (FabricLoader.getInstance().isDevelopmentEnvironment()) LOGGER.info("{} had been registered", Module.getStringName(module));

                    ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> module.onWorldJoin());
                    ClientPlayConnectionEvents.DISCONNECT.register(((handler, client) -> module.onWorldLeave()));

                    switch (module.getRunCategory()) {
                        case onStartingTick ->
                                ClientTickEvents.START_WORLD_TICK.register(world -> Modules.run(module));
                        case onEndingTick ->
                                ClientTickEvents.END_WORLD_TICK.register(world -> Modules.run(module));
                        case onStartingClientTick ->
                                ClientTickEvents.START_CLIENT_TICK.register(client -> Modules.run(module));
                        case onEndingClientTick ->
                                ClientTickEvents.END_CLIENT_TICK.register(client -> Modules.run(module));
                    }
                } else {
                    String name = clazz.getSimpleName();
                    LOGGER.error("Skipping {} as it is not a valid module !", name);
                    skippedModules++;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate {} : {}", clazz.getSimpleName(), e.getMessage(), e);
            }
        }

        long timeAtEnd = System.currentTimeMillis();

        if (skippedModules == 0)
            LOGGER.info("All modules were successfully instantiated in {} ms !", timeAtEnd - timeAtStart);
        else
            LOGGER.warn("Modules instantiated in {} ms. {} modules were skipped !", timeAtEnd - timeAtStart, skippedModules);

        Runtime.getRuntime().gc();
    }
}
