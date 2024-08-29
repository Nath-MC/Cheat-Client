package net.naxx.cheatmod.modules.movement;


import net.minecraft.block.BlockState;
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
import net.naxx.cheatmod.modules.Module;

import java.util.HashMap;

public final class ClickTP extends Module {
    private static final String description = "TP like an enderman";
    private final static HashMap<String, Float> settings = new HashMap<>();
    private final static RunCategory runCategory = RunCategory.onStartingClientTick;

    public ClickTP() {
        super(description, runCategory);
        settings.put("reach", 16F); //Default value
    }

    @Override
    public void run() {
        while (client.options.useKey.wasPressed()) {
            ItemStack mainHandItemStack = clientPlayer.getMainHandStack();
            HitResult hitResult = clientPlayer.raycast(settings.get("reach"), 1f / 20f, false);

            if (MathHelper.sqrt((float) hitResult.getPos().squaredDistanceTo(clientPlayer.getEyePos())) <= clientPlayer.getBlockInteractionRange() && !mainHandItemStack.isEmpty())
                return;

            if (hitResult.getType() == HitResult.Type.ENTITY && clientPlayer.interact(((EntityHitResult) hitResult).getEntity(), clientPlayer.preferredHand) != ActionResult.PASS)
                return;

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                Direction direction = ((BlockHitResult) hitResult).getSide();
                BlockState blockState = clientWorld.getBlockState(blockPos);

                if (blockState.onUse(clientWorld, clientPlayer, (BlockHitResult) hitResult) != ActionResult.PASS)
                    return;

                VoxelShape voxelShape = blockState.getCollisionShape(clientWorld, blockPos);

                if (voxelShape.isEmpty()) voxelShape = blockState.getOutlineShape(clientWorld, blockPos);

                double voxelShapeHeight = voxelShape.isEmpty() ? 1 : voxelShape.getMax(Direction.Axis.Y);

                Vec3d finalPosition = new Vec3d(blockPos.getX() + 0.5 + direction.getOffsetX(), blockPos.getY() + voxelShapeHeight, blockPos.getZ() + 0.5 + direction.getOffsetZ());
                Vec3d currentPosition = clientPlayer.getPos();

                for (int i = 0; i <= 6; i++) {
                    network.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(currentPosition.x, currentPosition.y, currentPosition.z, clientPlayer.isOnGround()));
                }

                network.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(finalPosition.x, finalPosition.y, finalPosition.z, clientPlayer.groundCollision));
                clientPlayer.setPosition(finalPosition);
            }
        }
    }
}
