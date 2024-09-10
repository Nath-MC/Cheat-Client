package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.RaycastContext;
import net.naxxsoftwares.mod.events.Event;
import net.naxxsoftwares.mod.modules.Module;

import java.util.HashMap;

public final class ClickTP extends Module {

    private final static HashMap<String, Float> SETTINGS = new HashMap<>();

    public ClickTP() {
        super("TP like an enderman");
        SETTINGS.put("reach", 16F); // Default value
    }

    @Event
    public void onClientTick(MinecraftClient client) {
        if (!client.options.useKey.wasPressed()) return;

        ItemStack mainHandItemStack = client.player.getMainHandStack();
        Vec3d playerEyePos = client.player.getEyePos();

        float maxDistance = SETTINGS.get("reach");
        Vec3d playerPos = client.player.getCameraPosVec(1F / 20F);
        Vec3d playerRotation = client.player.getRotationVector();
        Vec3d maxRaycastPos = playerPos.add(playerRotation.multiply(maxDistance));

        HitResult hitResult = client.world.raycast(new RaycastContext(playerPos, maxRaycastPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));

        // Handle entity interaction
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            ActionResult result = client.player.interact(entityHit.getEntity(), client.player.preferredHand);
            if (result != ActionResult.PASS) return;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHit.getBlockPos();
            Direction blockFace = blockHit.getSide();
            BlockState blockState = client.world.getBlockState(blockPos);

            // Check block interaction range and item in hand
            double distanceToHit = hitResult.getPos().squaredDistanceTo(playerEyePos);
            if (distanceToHit <= Math.pow(client.player.getBlockInteractionRange(), 2) && !mainHandItemStack.isEmpty()) return;

            // Interact with block
            ActionResult result = blockState.onUse(client.world, client.player, blockHit);
            if (result != ActionResult.PASS) return;


            VoxelShape collisionShape = blockState.getCollisionShape(client.world, blockPos);
            double shapeHeight = collisionShape.isEmpty() ? 1 : collisionShape.getMax(Direction.Axis.Y);

            Vec3d finalPos = new Vec3d(blockPos.getX() + 0.5 + blockFace.getOffsetX(), blockPos.getY() + shapeHeight, blockPos.getZ() + 0.5 + blockFace.getOffsetZ());

            client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(finalPos.x, finalPos.y, finalPos.z, client.player.isOnGround()));
            client.player.setPosition(finalPos);
        }
    }
}
