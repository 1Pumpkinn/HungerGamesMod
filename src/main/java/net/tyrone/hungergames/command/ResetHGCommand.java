package net.tyrone.hungergames.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.tyrone.hungergames.game.HGManager;

public class ResetHGCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("resethg")
                .requires(source -> source.hasPermissionLevel(2)) // Only ops
                .executes(ctx -> {
                    HGManager.resetGame(ctx.getSource().getServer());
                    return 1;
                }));
    }
}
