package net.tyrone.hungergames.listener;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.tyrone.hungergames.game.HGManager;

public class ServerTickHandler {
    public static void register() {
        ServerTickEvents.START_SERVER_TICK.register(HGManager::onServerTick);
    }
}
