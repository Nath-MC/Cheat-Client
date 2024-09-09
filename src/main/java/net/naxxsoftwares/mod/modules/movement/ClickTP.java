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
import net.minecraft.util.math.MathHelper;
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
        SETTINGS.put("reach", 12F); //Default value
    }

    // TODO 
    @Event
    public void onClientTick(MinecraftClient client) {
        while (client.options.useKey.wasPressed()) {
            ItemStack mainHandItemStack = client.player.getMainHandStack();

            float maxDistance = SETTINGS.get("reach");
            Vec3d vec3d = client.player.getCameraPosVec( 1F / 20F);
            Vec3d vec3d2 = client.player.getRotationVector();
            Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
            HitResult hitResult = client.world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));

            if (hitResult.getType() == HitResult.Type.ENTITY && client.player.interact(((EntityHitResult) hitResult).getEntity(), client.player.preferredHand) != ActionResult.PASS) return;
            if (MathHelper.sqrt((float) hitResult.getPos().squaredDistanceTo(client.player.getEyePos())) <= client.player.getBlockInteractionRange() && !mainHandItemStack.isEmpty()) return;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                Direction direction = ((BlockHitResult) hitResult).getSide();
                BlockState blockState = client.world.getBlockState(blockPos);

                if (blockState.onUse(client.world, client.player, (BlockHitResult) hitResult) != ActionResult.PASS) return;

                VoxelShape voxelShape = blockState.getCollisionShape(client.world, blockPos);

                double voxelShapeHeight = voxelShape.isEmpty() ? 1 : voxelShape.getMax(Direction.Axis.Y);

                Vec3d finalPosition = new Vec3d(blockPos.getX() + 0.5 + direction.getOffsetX(), blockPos.getY() + voxelShapeHeight, blockPos.getZ() + 0.5 + direction.getOffsetZ());

                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(finalPosition.x, finalPosition.y, finalPosition.z, client.player.groundCollision));
                client.player.setPosition(finalPosition);
            }
        }
    }
}
