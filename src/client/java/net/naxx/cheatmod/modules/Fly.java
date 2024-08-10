package net.naxx.cheatmod.modules;

import net.minecraft.block.AbstractBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.naxx.cheatmod.utils.chat.ChatUtils;

import java.util.Objects;


public class Fly {
    private static final Fly INSTANCE = new Fly();
    private final String NAME = "Fly";
    private final Text DESC = Text.of("it gives you some wiiings !");
    private final MinecraftClient client = MinecraftClient.getInstance();
    float speed = 0.1F;
    private Vec3d currentPos;
    private Vec3d lastPos;
    private boolean isModuleEnabled = false;
    private int floatingTicks = 0;
    private boolean floatingReset = false;


    public static Fly getINSTANCE() {
        return INSTANCE;
    }

    public void onTick() {
        if (!isModuleEnabled || client.player == null) return;

        ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();
        Objects.requireNonNull(networkHandler);

        lastPos = currentPos;
        currentPos = client.player.getPos();

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

            if (client.options.jumpKey.isPressed()) {
                velocity = velocity.add(0, getFinalSpeed(client.player), 0);
            }

            if (client.options.sneakKey.isPressed()) {
                velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);
            }

            if (client.player.input.pressingForward) {
                velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingBack) {
                velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingRight) {
                velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingLeft) {
                velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));
            }

            client.player.setVelocity(velocity);
            antiKick(networkHandler);
        } else if (client.player.getVehicle() instanceof BoatEntity boat) { //Boat fly mode
            boat.setYaw(client.player.getYaw());

            if (client.options.jumpKey.isPressed()) {
                velocity = velocity.add(0, getFinalSpeed(client.player) / 1.5F, 0);
            }

            if (client.options.sprintKey.isPressed()) {
                velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);
            }

            if (client.player.input.pressingForward) {
                velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingBack) {
                velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingRight) {
                velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingLeft) {
                velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));
            }

            boat.setVelocity(velocity);
            antiKick(networkHandler);
        } else if (client.player.isFallFlying()) { //Elytra fy mode
            if (client.options.jumpKey.isPressed()) {
                velocity = velocity.add(0, getFinalSpeed(client.player) / 1.5F, 0);
            }

            if (client.options.sneakKey.isPressed()) {
                velocity = velocity.subtract(0, getFinalSpeed(client.player), 0);
            }

            if (client.player.input.pressingForward) {
                velocity = velocity.add(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingBack) {
                velocity = velocity.subtract(f1 * getFinalSpeed(client.player), 0, f2 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingRight) {
                velocity = velocity.add(f3 * getFinalSpeed(client.player), 0, f4 * getFinalSpeed(client.player));
            }

            if (client.player.input.pressingLeft) {
                velocity = velocity.add(f5 * getFinalSpeed(client.player), 0, f6 * getFinalSpeed(client.player));
            }

            client.player.setVelocity(velocity);
        }

    }

    private void onActivate() {
        if (client.player == null) return;
        client.player.getAbilities().flying = false;
    }

    private void onDeactivate() {
        if (client.player == null) return;
        client.player.getAbilities().flying = false;
        client.player.getAbilities().allowFlying = false;
        client.player.setVelocity(0, 0, 0);
    }

    public String getName() {
        return NAME;
    }

    public Text getDESC() {
        return DESC;
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

    // See ServerPlayNetworkHandler#isEntityOnAir
    private boolean isEntityOnAir(Entity entity) {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    private void antiKick(ClientPlayNetworkHandler networkHandler) {
        if (isEntityOnAir(client.player) && !client.player.isFallFlying() && lastPos.y - currentPos.y < 0.03130D) { //Double value taken from the Flight module in the Meteor Project
            if (floatingTicks++ == 79) {
                floatingTicks = 0;
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(currentPos.x, currentPos.y - 0.03130D, currentPos.z, false)); //Double value taken from the Flight module in the Meteor Project
                floatingReset = true;
            } else if (floatingReset) {
                floatingReset = false;
                networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(lastPos.x, lastPos.y, lastPos.z, false));
            }
        } else {
            if (floatingReset) floatingReset = false;
            floatingTicks = 0;
        }
    }

    private float getFinalSpeed(ClientPlayerEntity player) {
        if (player.isFallFlying() || player.getVehicle() instanceof BoatEntity)
            return speed * 25F;
        if (player.isSprinting())
            return speed * 10F;
        return speed * 5F;
    }
}
