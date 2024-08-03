package net.naxx.cheatmod.modules;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

public class ClickTP {
    private static final ClickTP INSTANCE = new ClickTP();
    private final String NAME = "ClickTP";
    private boolean isModuleEnabled = false;

    public ClickTP() {
        ClientTickEvents.START_CLIENT_TICK.register((client -> {
            //Run at the start of each tick on the client-side
            if (client.player == null) return;

            if (this.isModuleEnabled() && client.options.useKey.wasPressed()) {
                ItemStack mainHandItemStack = client.player.getMainHandStack();
                HitResult hitResult = client.player.raycast(16, 1f / 20f, false);

                if (MathHelper.sqrt((float) client.player.squaredDistanceTo(hitResult.getPos())) <= 4.8 && !mainHandItemStack.isEmpty())
                    return;

                if (hitResult.getType() == HitResult.Type.ENTITY && client.player.interact(((EntityHitResult) hitResult).getEntity(), client.player.preferredHand) != ActionResult.PASS)
                    return;

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockPos = ((BlockHitResult) hitResult).getBlockPos();
                    Direction direction = ((BlockHitResult) hitResult).getSide();
                    BlockState blockState = client.world.getBlockState(blockPos);

                    if (blockState.onUse(client.world, client.player, (BlockHitResult) hitResult) != ActionResult.PASS)
                        return;

                    VoxelShape voxelShape = blockState.getCollisionShape(client.world, blockPos);

                    if (voxelShape.isEmpty()) voxelShape = blockState.getOutlineShape(client.world, blockPos);

                    double voxelShapeHeight = voxelShape.isEmpty() ? 1 : voxelShape.getMax(Direction.Axis.Y);

                    client.player.setPosition(blockPos.getX() + 0.5 + direction.getOffsetX(), blockPos.getY() + voxelShapeHeight, blockPos.getZ() + 0.5 + direction.getOffsetZ());
                }
            }
        }));
    }

    public static ClickTP getInstance() {
        return INSTANCE;
    }

    public void toggleModule() {
        isModuleEnabled = !isModuleEnabled;
    }

    public String getName() {
        return NAME;
    }

    public boolean isModuleEnabled() {
        return isModuleEnabled;
    }
}
