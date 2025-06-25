package net.tyrone.hungergames.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.tyrone.hungergames.game.HGManager;

public class StopHGCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("resethg")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    HGManager.resetGame(context.getSource().getServer());
                    return 1;
                }));
    }
}
