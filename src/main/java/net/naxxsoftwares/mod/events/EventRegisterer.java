package net.naxxsoftwares.mod.events;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.naxxsoftwares.mod.events.packet.PacketEvent;
import net.naxxsoftwares.mod.events.tick.TickEvents;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.RotationsUtils;
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
        if (isDevelopmentEnvironment) LOGGER.info("Registering events for \"{}\"", clazz.getSimpleName());

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
                    case PACKET -> PacketEvent.SEND.register((packet, event) -> {
                        if (packet instanceof PlayerPositionLookS2CPacket) RotationsUtils.reset();
                        invokeEventHandler(module, method, packet, event);
                    });
                    case WORLD_TICK -> TickEvents.WORLD_TICK.register(world -> invokeEventHandler(module, method, world));
                    case CLIENT_TICK -> TickEvents.CLIENT_TICK.register(client -> invokeEventHandler(module, method, client));
                    case WORLD_JOIN -> ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> invokeEventHandler(module, method));
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
        if (shouldInvokeMethodInModule(module)) {
            try {
                method.setAccessible(true);
                method.invoke(module, args);
            } catch (Exception e) {
                LoggerFactory.getLogger(EventRegisterer.class).error("Error invoking method: \"{}\" in \"{}\"", method.getName(), module.getClass(), e);
            }
        }
    }

    private static boolean shouldInvokeMethodInModule(@NotNull Module module) {
        return module.isActive() && client.player != null;
    }
}
