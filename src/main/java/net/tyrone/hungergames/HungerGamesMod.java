package net.tyrone.hungergames;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.tyrone.hungergames.command.ResetHGCommand;
import net.tyrone.hungergames.command.StartHGCommand;
import net.tyrone.hungergames.item.TNTCannonItem;
import net.tyrone.hungergames.listener.ServerTickHandler;

public class HungerGamesMod implements ModInitializer {
    public static Item TNT_CANNON;

    @Override
    public void onInitialize() {
        TNT_CANNON = Registry.register(Registries.ITEM, Identifier.of("hungergames", "tnt_cannon"),
                new TNTCannonItem(new Item.Settings().maxCount(1)));

        // Add item to creative inventory (optional)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(TNT_CANNON);
        });

        ServerTickHandler.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartHGCommand.register(dispatcher);
            ResetHGCommand.register(dispatcher);
        });
    }
}