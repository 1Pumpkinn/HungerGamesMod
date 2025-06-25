package net.tyrone.hungergames.registry;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tyrone.hungergames.HungerGamesMod;
import net.tyrone.hungergames.block.ElevatorBlockEntity;
import net.tyrone.hungergames.block.ElevatorBlock;

public class ModBlockEntities {

    public static BlockEntityType<ElevatorBlockEntity> ELEVATOR_BLOCK_ENTITY;

    public static void register() {
        ELEVATOR_BLOCK_ENTITY = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(HungerGamesMod.MOD_ID, "elevator_block_entity"),
                BlockEntityType.Builder.create(ElevatorBlockEntity::new, ModBlocks.ELEVATOR_BLOCK).build(null)
        );
    }
}
