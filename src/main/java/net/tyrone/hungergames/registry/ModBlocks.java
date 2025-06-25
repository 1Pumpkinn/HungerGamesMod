package net.tyrone.hungergames.registry;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tyrone.hungergames.HungerGamesMod;
import net.tyrone.hungergames.block.ElevatorBlock;
import net.tyrone.hungergames.block.ElevatorBlockEntity;

public class ModBlocks {

    public static final Block ELEVATOR_BLOCK = new ElevatorBlock(Block.Settings.create().strength(2.0f).nonOpaque());

    public static void register() {
        Registry.register(Registries.BLOCK, new Identifier(HungerGamesMod.MOD_ID, "elevator_block"), ELEVATOR_BLOCK);
    }
}
