package net.naxx.cheatmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.naxx.cheatmod.modules.Module;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Set;

public final class Initializer implements ClientModInitializer {
    public static final ModMetadata MOD_META;
    public static final String MOD_ID = "cheatmod";
    public static final String MOD_NAME;
    public static final Logger LOGGER;

    public static final MinecraftClient client = MinecraftClient.getInstance();

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();
        MOD_NAME = MOD_META.getName();
        LOGGER = LoggerFactory.getLogger(String.format("%s / %s", MOD_NAME, Initializer.class.getSimpleName()));
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Heyyy {} !", client.getGameProfile().getName());

        //Register modules and their event
        Reflections reflections = new Reflections("net.naxx.cheatmod.modules");
        Set<Class<? extends Module>> foundModules = reflections.getSubTypesOf(Module.class);

        for (Class<? extends Module> moduleClazz : foundModules) {
            if (Modifier.isAbstract(moduleClazz.getModifiers())) return;
            try {
                Module module = moduleClazz.getDeclaredConstructor().newInstance();

                switch (module.getRunCategory()) {
                    case onStartingTick:
                        ClientTickEvents.START_WORLD_TICK.register(world -> this.registerModule(module));
                        break;

                    case onEndingTick:
                        ClientTickEvents.END_WORLD_TICK.register(world -> this.registerModule(module));
                        break;

                    case OnStartingClientTick:
                        ClientTickEvents.START_CLIENT_TICK.register(client -> this.registerModule(module));
                        break;

                    case getOnEndingClientTick:
                        ClientTickEvents.END_CLIENT_TICK.register(client -> this.registerModule(module));
                        break;
                }

                ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> module.onWorldJoin(client.world));

                LOGGER.info("Registered {}", module.getName().getString());
            } catch (Exception e) {
                throw new ReflectionsException(e.getMessage());
            }
        }
        LOGGER.info("Modules successfully instantiated");
    }

    private void registerModule(Module module) {
        if (module.isActive() && client.player != null) module.run();
    }
}
