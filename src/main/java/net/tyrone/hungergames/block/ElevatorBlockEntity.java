package net.tyrone.hungergames.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class ElevatorBlockEntity extends BlockEntity {

    private int targetY = -1;
    private boolean movingUp = false;

    public ElevatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELEVATOR_BLOCK_ENTITY, pos, state);
    }

    public void moveToY(int y) {
        targetY = y;
        movingUp = y > pos.getY();
    }

    public void tick() {
        if (targetY == -1 || world == null || world.isClient) return;

        int currentY = pos.getY();

        if (currentY == targetY) {
            targetY = -1;
            return;
        }

        BlockPos newPos = pos.up(movingUp ? 1 : -1);
        BlockState state = world.getBlockState(pos);

        world.removeBlockEntity(pos);
        world.removeBlock(pos, false);

        world.setBlockState(newPos, state);
        BlockEntity newBE = world.getBlockEntity(newPos);
        if (newBE instanceof ElevatorBlockEntity newElevator) {
            newElevator.moveToY(targetY);
        }
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("TargetY", targetY);
        nbt.putBoolean("MovingUp", movingUp);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        targetY = nbt.getInt("TargetY");
        movingUp = nbt.getBoolean("MovingUp");
    }
}
