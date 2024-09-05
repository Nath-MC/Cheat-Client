package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.block.AbstractBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.utils.world.gamemode.GamemodeUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public final class Fly extends Module {

    private static final HashMap<String, Float> SETTINGS = new HashMap<>();
    private Vec3d currentPos;
    private Vec3d lastPos;
    private int floatingTicks;
    private boolean floatingReset;

    public Fly() {
        super("it gives you some wiiings !", RunType.onEndingTick);
        setSpeed(0.1F); //Default value
    }

    @Override
    public void onWorldJoin() {
        floatingTicks = -1;
        floatingReset = false;
    }

    @Override
    public void run() {
        if (GamemodeUtils.isInSpectator())
            return;
        if (currentPos == null)
            lastPos = client.player.getPos();
        else
            lastPos = currentPos;
        currentPos = client.player.getPos();

        //Math stuff : converting the current yaw to a 3d vector (so when u press w u go straight)
        float yawInRadians = (float) Math.toRadians(client.player.getYaw());
        float f1 = (float) -Math.sin(yawInRadians);
        float f2 = (float) Math.cos(yawInRadians);
        float f3 = (float) -Math.sin(yawInRadians + Math.PI / 2);
        float f4 = (float) Math.cos(yawInRadians + Math.PI / 2);
        float f5 = (float) -Math.sin(yawInRadians - Math.PI / 2);
        float f6 = (float) Math.cos(yawInRadians - Math.PI / 2);

        Vec3d velocity = Vec3d.ZERO;

        //Standard fly mode
        if (!client.player.isFallFlying() && !client.player.hasVehicle()) {

            if (client.options.jumpKey.isPressed())
                velocity = velocity.add(0, getFinalSpeed(client.player), 0);


            if (client.options.sneakKey.isPressed())
                velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);


            if (client.player.input.pressingForward)
                velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));


            if (client.player.input.pressingBack)
                velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));

            if (client.player.input.pressingRight)
                velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));

            if (client.player.input.pressingLeft)
                velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));

            client.player.setVelocity(velocity);
            client.player.setOnGround(true); // In order to prevent slow mining while flying. Consider using client.player.groundCollision && client.player.verticalCollision
            antiKick();
        } else if (client.player.getVehicle() instanceof BoatEntity boat) { //Boat fly mode
            boat.setYaw(client.player.getYaw());

            if (client.options.jumpKey.isPressed())
                velocity = velocity.add(0, getFinalSpeed(client.player) / 1.5F, 0);

            if (client.options.sprintKey.isPressed())
                velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);

            if (client.player.input.pressingForward)
                velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));

            if (client.player.input.pressingBack)
                velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));

            if (client.player.input.pressingRight)
                velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));

            if (client.player.input.pressingLeft)
                velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));

            boat.setVelocity(velocity);
            antiKick();
        } else if (client.player.isFallFlying()) { //Elytra fy mode

            if (client.options.jumpKey.isPressed())
                velocity = velocity.add(0, getFinalSpeed(client.player) / 1.5F, 0);

            if (client.options.sneakKey.isPressed())
                velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);

            if (client.player.input.pressingForward)
                velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));

            if (client.player.input.pressingBack)
                velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));

            if (client.player.input.pressingRight)
                velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));

            if (client.player.input.pressingLeft)
                velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));

            client.player.setVelocity(velocity);
        }

    }

    public void onActivate() {
        if (client.player == null)
            return;
        client.player.getAbilities().flying = false;
    }

    public void onDeactivate() {
        if (client.player == null)
            return;
        client.player.getAbilities().flying = false;
        client.player.getAbilities().allowFlying = false;
        client.player.setVelocity(0, 0, 0);
    }

    private void antiKick() {
        if (this.isEntityOnAir(client.player) && !client.player.isFallFlying() && !client.player.isSleeping() && !client.player.isDead() && lastPos.y - currentPos.y < 0.05 && !(client.player.verticalCollision && client.player.groundCollision) && GamemodeUtils.getOwnGamemode().isSurvivalLike()) {
            if (floatingTicks++ == 40) {
                floatingTicks = -1;
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(currentPos.x, (currentPos.y - 0.05), currentPos.z, client.player.isOnGround()));
                floatingReset = true;
            } else if (floatingReset) {
                floatingReset = false;
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(lastPos.x, lastPos.y, lastPos.z, client.player.isOnGround()));
            }
        } else {
            if (floatingReset)
                floatingReset = false;
            if (floatingTicks != -1)
                floatingTicks = -1;
        }
    }

    // See ServerPlayNetworkHandler#isEntityOnAir
    private boolean isEntityOnAir(@NotNull Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625D).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    private float getFinalSpeed(@NotNull ClientPlayerEntity player) {
        if (player.isFallFlying() || player.getVehicle() instanceof BoatEntity)
            return getSpeed() * 25F;
        if (client.options.sprintKey.isPressed() || player.isSprinting())
            return getSpeed() * 10F;
        return getSpeed() * 5F;
    }

    private float getSpeed() {
        return SETTINGS.get("speed");
    }

    private void setSpeed(float value) {
        SETTINGS.put("speed", value);
    }
}
