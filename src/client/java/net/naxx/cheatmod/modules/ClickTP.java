package net.naxx.cheatmod.modules;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.naxx.cheatmod.utils.chat.ChatUtils;

import java.util.Objects;

public class ClickTP {
    private static final ClickTP INSTANCE = new ClickTP();
    private final String NAME = "ClickTP";
    private final Text DESC = Text.of("Teleport yourself toward a block");
    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean isModuleEnabled = false;
    private int reach = 16;

    public static ClickTP getInstance() {
        return INSTANCE;
    }

    public void onTick() {
        if (this.isModuleEnabled() && client.options.useKey.wasPressed() && client.player != null) {
            Objects.requireNonNull(client.getNetworkHandler());

            ItemStack mainHandItemStack = client.player.getMainHandStack();
            HitResult hitResult = client.player.raycast(reach, 1f / 20f, false);

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

                Vec3d aimedPosition = new Vec3d(blockPos.getX() + 0.5 + direction.getOffsetX(), blockPos.getY() + voxelShapeHeight, blockPos.getZ() + 0.5 + direction.getOffsetZ());
                Vec3d currentPosition = client.player.getPos();

                for (int i = 0; i < 5; i++) {
                    client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(currentPosition.x, currentPosition.y, currentPosition.z, client.player.isOnGround()));
                }

                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(aimedPosition.x, aimedPosition.y, aimedPosition.z, client.player.groundCollision));
                client.player.setPosition(aimedPosition);
            }
        }
    }

    public boolean isModuleEnabled() {
        return isModuleEnabled;
    }

    public String getName() {
        return NAME;
    }

    public Text getDESC() {
        return DESC;
    }

    public void toggleModule() {
        isModuleEnabled = !isModuleEnabled;
        ChatUtils.sendMessage(String.format("§l%s§r is %s", NAME, isModuleEnabled ? "§aon§r" : "§coff§r"));
    }

    public int getReach() {
        return reach;
    }

    public void setReach(int newReach) {
        reach = newReach;
    }
}
