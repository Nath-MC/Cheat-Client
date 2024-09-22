package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.util.math.Vec3d;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;
import net.naxxsoftwares.mod.modules.Modules;

public final class VHop extends Module {

    public VHop() {
        super("Make you turn into a bunny");
    }

    @Event
    public void onEndingTick() {
        if (Modules.isModuleActive(Fly.class) || !isMoving()) return;
        if (client.player.groundCollision && client.player.verticalCollision) { // Collision under the player == on ground
            client.player.setSprinting(false);
            client.player.jump();
        } else if (!client.player.isInFluid() && !client.player.isSneaking()) {
            float yawInRadians = (float) Math.toRadians(client.player.getYaw());
            Vec3d velocity = getVelocity(yawInRadians);
            client.player.setVelocity(velocity);
        }
    }

    private boolean isMoving() {
        return client.options.forwardKey.isPressed() || client.options.backKey.isPressed() || client.options.leftKey.isPressed() || client.options.rightKey.isPressed();
    }

    private Vec3d getVelocity(float yawInRadians) {
        //Math stuff : converting the current yaw to a 3d vector (so when u press w u go straight)
        float f1 = (float) -Math.sin(yawInRadians);
        float f2 = (float) Math.cos(yawInRadians);
        float f3 = (float) -Math.sin(yawInRadians + Math.PI / 2);
        float f4 = (float) Math.cos(yawInRadians + Math.PI / 2);
        float f5 = (float) -Math.sin(yawInRadians - Math.PI / 2);
        float f6 = (float) Math.cos(yawInRadians - Math.PI / 2);

        Vec3d velocity = Vec3d.ZERO;
        float speed = 0.2F;

        velocity = velocity.add(0, client.player.getVelocity().getY(), 0);

        if (client.options.forwardKey.isPressed()) velocity = velocity.add(f1 * speed, 0, f2 * speed);

        if (client.options.backKey.isPressed()) velocity = velocity.subtract(f1 * speed, 0, f2 * speed);

        if (client.options.rightKey.isPressed()) velocity = velocity.add(f3 * speed, 0, f4 * speed);

        if (client.options.leftKey.isPressed()) velocity = velocity.add(f5 * speed, 0, f6 * speed);
        return velocity;
    }
}
