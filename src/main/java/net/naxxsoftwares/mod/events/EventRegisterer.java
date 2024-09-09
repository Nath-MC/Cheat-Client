package net.naxxsoftwares.mod.events;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.naxxsoftwares.mod.events.packet.PacketEvent;
import net.naxxsoftwares.mod.events.tick.TickEvents;
import net.naxxsoftwares.mod.modules.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static net.naxxsoftwares.mod.Initializer.client;
import static net.naxxsoftwares.mod.Initializer.isDevelopmentEnvironment;

public class EventRegisterer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRegisterer.class);

    // Register all methods annotated with @Event
    public static void registerEvents(@NotNull Module module) {
        Class<?> clazz = module.getClass();

        // Loop through all declared methods in the class
        for (Method method : clazz.getDeclaredMethods()) {

            // Check if the method is annotated with @Event
            if (method.isAnnotationPresent(Event.class)) {

                // Get the event type from the method name
                EventType eventType = inferEventType(method.getName());
                if (eventType == null) {
                    LOGGER.error("Method \"{}\" in \"{}\" does not match any known event types", method.getName(), Module.getStringName(module));
                    continue;
                }

                switch (eventType) {
                    case PACKET -> registerPacketEventFor(module, method);
                    case WORLD_TICK -> registerWorldTickEventFor(module, method);
                    case CLIENT_TICK -> registerClientTickEventFor(module, method);
                    case WORLD_JOIN -> registerWorldJoinEventFor(module, method);
                }
            }
        }
    }

    public static @Nullable EventType inferEventType(@NotNull String methodName) {
        return switch (methodName) {
            case "onTick" -> EventType.WORLD_TICK;
            case "onClientTick" -> EventType.CLIENT_TICK;
            case "onPacket" -> EventType.PACKET;
            case "onWorldJoin" -> EventType.WORLD_JOIN;
            default -> null;
        };

    }

    private static void invokeEventHandler(Module module, Method method, Object... args) {
        if (shouldInvoke(module)) {
            try {
                method.setAccessible(true);
                method.invoke(module, args);
            } catch (Exception e) {
                LoggerFactory.getLogger(EventRegisterer.class).error("Error invoking method: \"{}\" in \"{}\"", method.getName(), module.getClass(), e);
            }
        }
    }

    private static boolean shouldInvoke(@NotNull Module module) {
        return module.isActive() && client.player != null;
    }

    private static void registerPacketEventFor(Module module, Method method) {
        if (isDevelopmentEnvironment) LOGGER.info("Registered a packet event for \"{}\"", Module.getStringName(module));
        PacketEvent.SEND.register((packet, event) -> invokeEventHandler(module, method, packet, event));
    }

    private static void registerWorldTickEventFor(Module module, Method method) {
        if (isDevelopmentEnvironment) LOGGER.info("Registered a world tick event for \"{}\"", Module.getStringName(module));
        TickEvents.WORLD_TICK.register(world -> invokeEventHandler(module, method, world));
    }

    private static void registerClientTickEventFor(Module module, Method method) {
        if (isDevelopmentEnvironment) LOGGER.info("Registered a client tick event for \"{}\"", Module.getStringName(module));
        TickEvents.CLIENT_TICK.register(client -> invokeEventHandler(module, method, client));
    }

    private static void registerWorldJoinEventFor(Module module, Method method) {
        if (isDevelopmentEnvironment) LOGGER.info("Registered a world join event for \"{}\"", Module.getStringName(module));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> invokeEventHandler(module, method));
    }
}