package net.naxxsoftwares.mod.modules.movement;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
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
import net.naxxsoftwares.mod.utils.PositionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class ClickTP extends Module {

    private final static HashMap<String, Float> SETTINGS = new HashMap<>();

    public ClickTP() {
        super("TP like an enderman");
        SETTINGS.put("reach", 36F); // Default value
    }

    @Event
    public void onClientTick(MinecraftClient client) {
        if (!client.options.useKey.wasPressed()) return;

        ItemStack itemStack = client.player.getMainHandStack();
        Vec3d playerEyePos = client.player.getEyePos();
        Vec3d playerRotation = client.player.getRotationVector();
        Vec3d maxRaycastPos = playerEyePos.add(playerRotation.multiply(SETTINGS.get("reach")));
        HitResult hitResult = client.world.raycast(new RaycastContext(playerEyePos, maxRaycastPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, client.player));

        // Handle entity interaction
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) hitResult;
            ActionResult result = client.player.interact(entityHit.getEntity(), client.player.preferredHand);
            if (result != ActionResult.PASS) return;
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState blockState = client.world.getBlockState(blockPos);

            //If the block is reachable and if the held item is a block or if the block can be interacted with, skip.
            if (isBlockInReach(blockPos) && (itemStack.getItem() instanceof BlockItem || blockState.onUse(client.world, client.player, blockHit) != ActionResult.PASS)) return;
            if (!canTPAbove(client.world, blockPos)) return;

            VoxelShape collisionShape = blockState.getCollisionShape(client.world, blockPos);
            Vec3d finalPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + collisionShape.getMax(Direction.Axis.Y), blockPos.getZ() + 0.5 );
            PositionUtils.lerpPositionPacket(client.player.getPos(), finalPos, false, 5);
        }
    }

    private boolean isBlockInReach(@NotNull BlockPos blockPos) {
       return blockPos.getSquaredDistance(client.player.getEyePos()) <= Math.pow(client.player.getBlockInteractionRange(), 2);
    }

    private boolean canTPAbove(ClientWorld world, BlockPos pos) {
        for (int i = 1; i <= 2; i++) {
            BlockState state = world.getBlockState(pos.up(i));
            VoxelShape shape = state.getCollisionShape(world, pos.up(i));
           if (!shape.isEmpty()) return false;
        }
        return true;
    }

}
