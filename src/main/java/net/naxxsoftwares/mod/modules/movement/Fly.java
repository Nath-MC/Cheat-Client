package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.block.AbstractBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.GamemodeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public final class Fly extends Module {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    private static Vec3d currentPos, lastPos;
    private static int floatingTicks;
    private static boolean wereFloatingTicksReset;
    private static byte cycles;

    public Fly() {
        super("it gives you some wiiings !");
        setSpeed(0.25F); //Default value
    }

    @Event
    public static void onWorldJoin() {
        floatingTicks = 0;
        wereFloatingTicksReset = false;
    }

    @Event
    public void onEndingTick() {
        if (GamemodeUtils.isInSpectator()) return;
        switch (getFlyingType()) {
            case STANDARD, ELYTRA -> setEntityVelocityBasedOnInput(client.player);
            case BOAT -> {
                BoatEntity boat = (BoatEntity) client.player.getVehicle();
                boat.setYaw(client.player.getYaw());
                setEntityVelocityBasedOnInput(boat);
            }
        }
    }

    private float getFinalSpeed(@NotNull ClientPlayerEntity player) {
        if (player.isFallFlying() || player.getVehicle() instanceof BoatEntity) return getSpeed() * 25F;
        if (client.options.sprintKey.isPressed() || player.isSprinting()) return getSpeed() * 10F;
        return getSpeed() * 5F;
    }

    private float getSpeed() {
        return SETTINGS.get("speed");
    }

    private void setSpeed(float value) {
        SETTINGS.put("speed", value);
    }

    private FlyTypes getFlyingType() {
        if (client.player.getVehicle() instanceof BoatEntity) return FlyTypes.BOAT;
        if (client.player.isFallFlying()) return FlyTypes.ELYTRA;
        return FlyTypes.STANDARD;
    }

    private void setEntityVelocityBasedOnInput(Entity entity) {
        Vec3d velocity = Vec3d.ZERO;

        //Converting the current yaw to a 3d vector (so when u press w u go straight)
        float yawInRadians = (float) Math.toRadians(client.player.getYaw());
        float f1 = (float) -Math.sin(yawInRadians);
        float f2 = (float) Math.cos(yawInRadians);
        float f3 = (float) -Math.sin(yawInRadians + Math.PI / 2);
        float f4 = (float) Math.cos(yawInRadians + Math.PI / 2);
        float f5 = (float) -Math.sin(yawInRadians - Math.PI / 2);
        float f6 = (float) Math.cos(yawInRadians - Math.PI / 2);

        if (client.options.jumpKey.isPressed()) velocity = velocity.add(0, getFinalSpeed(client.player) / 1.5F, 0);
        if (client.options.sneakKey.isPressed()) velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);

        if (client.player.input.pressingForward) velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
        if (client.player.input.pressingBack) velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));

        if (client.player.input.pressingRight) velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));
        if (client.player.input.pressingLeft) velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));

        entity.setVelocity(velocity);
    }

    public void onActivate() {
        if (client.player == null) return;
        client.player.getAbilities().flying = false;
    }

    public void onDeactivate() {
        if (client.player == null) return;
        client.player.getAbilities().flying = false;
        client.player.getAbilities().allowFlying = false;
        client.player.setVelocity(0, 0, 0);
    }

    enum FlyTypes {
        STANDARD, BOAT, ELYTRA
    }
}
