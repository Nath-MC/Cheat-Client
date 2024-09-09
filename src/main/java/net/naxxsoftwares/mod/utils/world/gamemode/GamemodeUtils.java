package net.naxxsoftwares.mod.utils.world.gamemode;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameMode;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;


public abstract class GamemodeUtils {
    private static final MinecraftClient client = Initializer.client;
    private static final @Nullable ClientPlayerEntity player = client.player;
    private static final @Nullable ClientPlayNetworkHandler network = client.getNetworkHandler();

    public static @NotNull GameMode getGamemode(@NotNull Entity entity) {
        return getGamemode(entity.getUuid());
    }

    public static @NotNull GameMode getGamemode(UUID uuid) {
        assert !Objects.isNull(player) && !Objects.isNull(network);
        return Objects.requireNonNull(network.getPlayerListEntry(uuid), "failed to resolve target UUID").getGameMode();
    }

    public static boolean isInSpectator() {
        return getOwnGamemode() == GameMode.SPECTATOR;
    }

    public static @NotNull GameMode getOwnGamemode() {
        assert client.interactionManager != null;
        return Objects.requireNonNull(client.interactionManager.getCurrentGameMode(), "failed to get own gamemode");
    }
}
