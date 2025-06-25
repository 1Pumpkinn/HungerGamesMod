package net.tyrone.hungergames;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.tyrone.hungergames.command.ResetHGCommand;
import net.tyrone.hungergames.command.StartHGCommand;
import net.tyrone.hungergames.listener.ServerTickHandler;

public class HungerGamesMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Tick logic (like elevator rising and grace period)
        ServerTickHandler.register();

        // Command logic
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            StartHGCommand.register(dispatcher);
            ResetHGCommand.register(dispatcher);
        });
    }
}
