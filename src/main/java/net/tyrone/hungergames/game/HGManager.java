package net.tyrone.hungergames.game;

import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.*;

public class HGManager {

    private static final List<BlockPos> spawnPoints = Arrays.asList(
            new BlockPos(0, 100, 0),
            new BlockPos(10, 100, 0),
            new BlockPos(-10, 100, 0),
            new BlockPos(0, 100, 10),
            new BlockPos(0, 100, -10),
            new BlockPos(10, 100, 10),
            new BlockPos(-10, 100, -10),
            new BlockPos(-10, 100, 10),
            new BlockPos(10, 100, -10)
    );

    private static final Set<UUID> activePlayers = new HashSet<>();
    private static boolean gameRunning = false;
    private static boolean pvpEnabled = false;
    private static int gracePeriodTicks = -1;

    public static boolean isGameRunning() {
        return gameRunning;
    }

    public static boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public static void startGame(MinecraftServer server) {
        if (gameRunning) return;

        gameRunning = true;
        pvpEnabled = false;
        activePlayers.clear();

        List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
        Collections.shuffle(players);

        int spawnCount = Math.min(players.size(), spawnPoints.size());
        for (int i = 0; i < spawnCount; i++) {
            ServerPlayerEntity player = players.get(i);
            BlockPos pos = spawnPoints.get(i);

            // Build launch tube
            buildLaunchPlatform(server.getOverworld(), pos);

            // Teleport into the tube
            player.networkHandler.requestTeleport(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, player.getYaw(), player.getPitch());
            player.changeGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.getInventory().clear();
            player.sendMessage(Text.literal("You are in the launch tube. Prepare for the Hunger Games."), false);
            activePlayers.add(player.getUuid());
        }

        generateCornucopiaLoot(server.getOverworld());

        server.getPlayerManager().broadcast(Text.literal("§6Hunger Games has begun! Good luck tributes."), false);
        server.getPlayerManager().broadcast(Text.literal("§7[Grace Period] PvP will be enabled in 15 seconds..."), false);

        gracePeriodTicks = 15 * 20; // 15 seconds
    }

    public static void resetGame(MinecraftServer server) {
        gameRunning = false;
        pvpEnabled = false;
        activePlayers.clear();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.sendMessage(Text.literal("§7Hunger Games has been reset."), false);
        }
    }

    public static void playerDied(MinecraftServer server, ServerPlayerEntity player) {
        if (!activePlayers.contains(player.getUuid())) return;

        activePlayers.remove(player.getUuid());

        // Spectator mode
        player.changeGameMode(GameMode.SPECTATOR);
        server.getPlayerManager().broadcast(Text.literal("§c" + player.getName().getString() + " has fallen."), false);

        checkForWinner(server);
    }

    public static void checkForWinner(MinecraftServer server) {
        if (activePlayers.size() == 1) {
            UUID winnerId = activePlayers.iterator().next();
            ServerPlayerEntity winner = null;

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getUuid().equals(winnerId)) {
                    winner = player;
                    break;
                }
            }

            if (winner != null) {
                server.getPlayerManager().broadcast(Text.literal("§a" + winner.getName().getString() + " has won the Hunger Games!"), false);
                winner.sendMessage(Text.literal("§6You are the victor!"), false);
            }

            gameRunning = false;
            pvpEnabled = false;
            activePlayers.clear();
        } else if (activePlayers.isEmpty()) {
            server.getPlayerManager().broadcast(Text.literal("§cAll tributes have fallen. No one wins..."), false);
            gameRunning = false;
            pvpEnabled = false;
        }
    }

    private static void generateCornucopiaLoot(ServerWorld world) {
        BlockPos center = new BlockPos(0, 99, 0);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = center.add(dx, 0, dz);
                world.setBlockState(pos, Blocks.CHEST.getDefaultState());

                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof net.minecraft.block.entity.ChestBlockEntity chest) {
                    chest.setStack(0, Items.IRON_SWORD.getDefaultStack());
                    chest.setStack(1, Items.BREAD.getDefaultStack());
                    chest.setStack(2, Items.GOLDEN_APPLE.getDefaultStack());
                }
            }
        }
    }

    private static void buildLaunchPlatform(ServerWorld world, BlockPos basePos) {
        // Clear space and build glass tube
        for (int y = 0; y < 4; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = basePos.add(dx, y, dz);
                    boolean isEdge = dx == -1 || dx == 1 || dz == -1 || dz == 1;
                    if (isEdge) {
                        world.setBlockState(pos, Blocks.GLASS.getDefaultState());
                    } else {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        // Place gold launch platform
        world.setBlockState(basePos, Blocks.GOLD_BLOCK.getDefaultState());
    }

    public static void onServerTick(MinecraftServer server) {
        if (gracePeriodTicks > 0) {
            gracePeriodTicks--;
            if (gracePeriodTicks == 0) {
                pvpEnabled = true;
                server.getPlayerManager().broadcast(Text.literal("§cGrace period is over! PvP is now enabled."), false);

                // Remove glass tubes
                for (BlockPos basePos : spawnPoints) {
                    for (int y = 1; y <= 3; y++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                boolean isEdge = dx == -1 || dx == 1 || dz == -1 || dz == 1;
                                if (isEdge) {
                                    BlockPos glassPos = basePos.add(dx, y, dz);
                                    server.getOverworld().setBlockState(glassPos, Blocks.AIR.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
