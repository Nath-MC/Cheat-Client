package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.block.AbstractBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.mixins.ClientPlayerEntityAccessor;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.GamemodeUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;


public final class Fly extends Module {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    private static Vec3d currentPos, lastPos;
    private static int floatingTicks;

    public Fly() {
        super("it gives you some wiiings !");
        setSpeed(0.25F); //Default value
    }

    @Event
    public void onWorldJoin() {
        floatingTicks = 0;
    }

    @Event
    public void onPacket(Packet<?> packet, CallbackInfo event) {
        if (packet instanceof PlayerMoveC2SPacket movePacket && floatingTicks >= 40) {
            floatingTicks = 0;
            ((ClientPlayerEntityAccessor) client.player).setTicksSinceLastPositionPacketSent(20);
            event.cancel();
            PlayerMoveC2SPacket newPacket = offsetYInPacket(movePacket);
            client.getNetworkHandler().sendPacket(newPacket);
        }
    }

    private PlayerMoveC2SPacket offsetYInPacket(@NotNull PlayerMoveC2SPacket packet) {
        double offset = (packet.changesPosition() ? packet.getY(Double.MAX_VALUE) : currentPos.y) - 0.03130D;

        if (packet.changesPosition()) {
            if (packet instanceof PlayerMoveC2SPacket.Full)
                return new PlayerMoveC2SPacket.Full(packet.getX(Double.MAX_VALUE), offset, packet.getZ(Double.MAX_VALUE), packet.getYaw(Float.MAX_VALUE), packet.getPitch(Float.MAX_VALUE), packet.isOnGround());
            if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround) return new PlayerMoveC2SPacket.PositionAndOnGround(packet.getX(Double.MAX_VALUE), offset, packet.getZ(Double.MAX_VALUE), packet.isOnGround());
        }

        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround)
            return new PlayerMoveC2SPacket.Full(currentPos.x, offset, currentPos.z, packet.getYaw(Float.MAX_VALUE), packet.getPitch(Float.MAX_VALUE), packet.isOnGround());

        // OnGroundOnly packet
        return new PlayerMoveC2SPacket.PositionAndOnGround(currentPos.x, offset, currentPos.z, packet.isOnGround());
    }

    @Event
    public void onEndingTick() {
        if (GamemodeUtils.isInSpectator()) return;

        updatePosition();
        updateFloatingTicks();

        switch (getFlyingType()) {
            case STANDARD, ELYTRA -> setEntityVelocityBasedOnInput(client.player);
            case BOAT -> {
                BoatEntity boat = (BoatEntity) client.player.getVehicle();
                boat.setYaw(client.player.getYaw());
                setEntityVelocityBasedOnInput(boat);
            }
        }
    }

    private void updateFloatingTicks() {
        if (isFloating()) floatingTicks++;
        else if (floatingTicks != 0) floatingTicks = 0;
    }

    private boolean isFloating() {
        return !client.isInSingleplayer() && GamemodeUtils.getOwnGamemode().isSurvivalLike() && client.player.isAlive() && !client.player.isSleeping() && !client.player.isFallFlying() && isPlayerOnAir(client.player) && lastPos.y - currentPos.y < 0.03130D;
    }

    // See ServerPlayNetworkHandler#isEntityOnAir
    private boolean isPlayerOnAir(@NotNull ClientPlayerEntity player) {
        return player.getWorld().getStatesInBox(player.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    private void updatePosition() {
        if (currentPos == null) {
            lastPos = currentPos = client.player.getPos();
            return;
        }

        lastPos = currentPos;
        currentPos = client.player.getPos();
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

    public void onActivate() {
        if (client.player == null) return;
        client.player.getAbilities().flying = false;
    }

    public void onDeactivate() {
        if (client.player == null) return;
        client.player.setVelocity(0, 0, 0);
        if (!GamemodeUtils.getOwnGamemode().isCreative()) return;
        client.player.getAbilities().flying = !client.player.groundCollision;
        client.player.getAbilities().allowFlying = true;
    }

    enum FlyTypes {
        STANDARD, BOAT, ELYTRA
    }
}
