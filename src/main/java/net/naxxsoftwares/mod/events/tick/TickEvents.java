package net.naxxsoftwares.mod.events.tick;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public abstract class TickEvents {

    public static final Event<ClientTick> CLIENT_TICK = EventFactory.createArrayBacked(ClientTick.class, callbacks -> client -> {
        for (ClientTick event : callbacks)
            event.onClientTick(client);
    });

    public static final Event<WorldTick> WORLD_TICK = EventFactory.createArrayBacked(WorldTick.class, callbacks -> world -> {
        for (WorldTick event : callbacks)
            event.onWorldTick(world);
    });

    @FunctionalInterface
    public interface ClientTick {
        void onClientTick(MinecraftClient client);
    }

    @FunctionalInterface
    public interface WorldTick {
        void onWorldTick(ClientWorld world);
    }
}
