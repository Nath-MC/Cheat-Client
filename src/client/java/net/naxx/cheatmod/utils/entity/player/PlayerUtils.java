package net.naxx.cheatmod.utils.entity.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameMode;
import net.naxx.cheatmod.Initializer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;


public abstract class PlayerUtils {
    private static final MinecraftClient client = Initializer.client;
    private static final ClientPlayerEntity player = client.player;
    private static final ClientPlayNetworkHandler network = client.getNetworkHandler();


    public static @NotNull GameMode getGamemode(UUID uuid) {
        assert !Objects.isNull(player) && !Objects.isNull(network);
        return Objects.requireNonNull(network.getPlayerListEntry(uuid), "failed to get target gamemode").getGameMode();
    }

    public static @NotNull GameMode getGamemode(@NotNull Entity entity) {
        return getGamemode(entity.getUuid());
    }

    public static @NotNull GameMode getOwnGamemode() {
        assert client.player != null;
        return Objects.requireNonNull(client.interactionManager.getCurrentGameMode(), "failed to get own gamemode");
    }
}
