package net.naxxsoftwares.mod.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.naxxsoftwares.mod.Initializer;
import org.jetbrains.annotations.NotNull;


public abstract class ChatUtils {
    private static final MinecraftClient client = Initializer.client;
    private static final String PREFIX = "§d§lCheatMod§r  §l|§r  ";

    public static void sendClientMessage(@NotNull String message, Object... args) {
        client.inGameHud.getChatHud().addMessage(Text.of(PREFIX + String.format(message, args)));
    }
}
