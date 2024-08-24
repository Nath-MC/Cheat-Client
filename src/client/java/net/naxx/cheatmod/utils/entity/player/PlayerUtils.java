package net.naxx.cheatmod.utils.entity.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.GameMode;
import net.naxx.cheatmod.Initializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;


public abstract class PlayerUtils {
    private static final MinecraftClient client = Initializer.client;
    private static final ClientPlayerEntity player = client.player;
    private static final ClientPlayNetworkHandler network = client.getNetworkHandler();


    public static @Nullable GameMode getGamemode(UUID uuid) {
        assert !Objects.isNull(player) && !Objects.isNull(network);
        return Objects.requireNonNull(network.getPlayerListEntry(uuid), "failed to get target gamemode").getGameMode();
    }

    public static @Nullable GameMode getGamemode(@NotNull Entity entity) {
        return getGamemode(entity.getUuid());
    }

    public static @Nullable GameMode getOwnGamemode() {
        assert client.player != null;
        return client.interactionManager.getCurrentGameMode();
    }
}
