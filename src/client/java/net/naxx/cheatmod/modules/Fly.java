package net.naxx.cheatmod.modules;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.naxx.cheatmod.utils.chat.ChatUtils;

public class Fly {
    private static final Fly INSTANCE = new Fly();
    private final String NAME = "Fly";
    private boolean isModuleEnabled = false;

    private ClientPlayerEntity player = MinecraftClient.getInstance().player;
    private double speed = 1.5;

    public Fly() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isModuleEnabled || player == null) return;

            player.setVelocity(0, 0, 0);
            Vec3d velocity = Vec3d.ZERO;

            if (client.options.jumpKey.isPressed()) {
                velocity = velocity.add(0, speed, 0);
            } else if (client.options.sneakKey.isPressed()) {
                velocity = velocity.subtract(0, speed, 0);
            }

            // Handle horizontal movement
            if (player.input.pressingForward) {
                velocity = velocity.add(player.getRotationVec(1.0F).multiply(speed)); // Adjust the value for desired speed
            } else if (player.input.pressingBack) {
                velocity = velocity.add(player.getRotationVec(1.0F).multiply(speed)); // Adjust the value for desired speed
            }

            if (player.input.pressingRight) {
                Vec3d leftVec = player.getRotationVec(1.0F).rotateY((float) Math.toRadians(-90)).normalize();
                velocity = velocity.add(leftVec.multiply(speed)); // Adjust the value for desired speed
            } else if (player.input.pressingLeft) {
                Vec3d rightVec = player.getRotationVec(1.0F).rotateY((float) Math.toRadians(90)).normalize();
                velocity = velocity.add(rightVec.multiply(speed)); // Adjust the value for desired speed
            }

            player.setVelocity(velocity);

        });
    }

    private void onActivate() {
        if (player == null) return;
        player.getAbilities().flying = false;
    }

    private void onDeactivate() {
        if (player == null) return;
        player.getAbilities().flying = false;
        player.getAbilities().allowFlying = false;
        player.setVelocity(0, 0, 0);
    }

    public static Fly getINSTANCE() {
        return INSTANCE;
    }

    public String getName() {
        return NAME;
    }

    public boolean isModuleEnabled() {
        return isModuleEnabled;
    }

    public void toogleModule() {
        isModuleEnabled = !isModuleEnabled;
        ChatUtils.sendMessage(String.format("§l%s§r is %s", NAME, isModuleEnabled ? "§aon§r" : "§coff§r"));

        if (isModuleEnabled) onActivate();
        else onDeactivate();
    }
}
