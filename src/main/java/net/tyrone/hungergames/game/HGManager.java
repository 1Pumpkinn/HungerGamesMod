package net.tyrone.hungergames.game;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.*;

public class HGManager {

    private static final List<BlockPos> spawnPoints = new ArrayList<>();
    private static final Set<UUID> activePlayers = new HashSet<>();
    private static final Map<BlockPos, Integer> elevatorProgress = new HashMap<>();
    private static boolean gameRunning = false;
    private static boolean pvpEnabled = false;
    private static int gracePeriodTicks = -1;
    private static final int MAX_ELEVATOR_HEIGHT = 5;

    static {
        double radius = 20;
        for (int i = 0; i < 24; i++) {
            double angle = 2 * Math.PI * i / 24;
            int x = (int) Math.round(Math.cos(angle) * radius);
            int z = (int) Math.round(Math.sin(angle) * radius);
            spawnPoints.add(new BlockPos(x, 100, z));
        }
    }

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
        elevatorProgress.clear();

        List<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
        Collections.shuffle(players);

        ServerWorld world = server.getOverworld();
        int spawnCount = Math.min(players.size(), spawnPoints.size());

        for (int i = 0; i < spawnCount; i++) {
            ServerPlayerEntity player = players.get(i);
            BlockPos basePos = spawnPoints.get(i);

            buildLaunchTube(world, basePos);
            player.networkHandler.requestTeleport(basePos.getX() + 0.5, basePos.getY(), basePos.getZ() + 0.5, player.getYaw(), player.getPitch());
            player.changeGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.getInventory().clear();
            player.sendMessage(Text.literal("You are in the launch tube. Prepare for the Hunger Games."), false);
            activePlayers.add(player.getUuid());

            elevatorProgress.put(basePos, 0); // Animate platform rise
        }

        generateCornucopiaLoot(world);

        server.getPlayerManager().broadcast(Text.literal("§6Hunger Games has begun! Good luck tributes."), false);
        server.getPlayerManager().broadcast(Text.literal("§7[Grace Period] PvP will be enabled in 15 seconds..."), false);
        gracePeriodTicks = 15 * 20; // 15 seconds
    }

    public static void resetGame(MinecraftServer server) {
        gameRunning = false;
        pvpEnabled = false;
        activePlayers.clear();
        elevatorProgress.clear();

        ServerWorld world = server.getOverworld();

        for (BlockPos basePos : spawnPoints) {
            for (int y = 0; y <= MAX_ELEVATOR_HEIGHT + 3; y++) {
                world.setBlockState(basePos.up(y), Blocks.AIR.getDefaultState());
            }
            world.setBlockState(basePos, Blocks.GOLD_BLOCK.getDefaultState());
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.changeGameMode(GameMode.ADVENTURE);
            player.getInventory().clear();
            player.sendMessage(Text.literal("§7Hunger Games has been reset."), false);
        }
    }

    public static void playerDied(MinecraftServer server, ServerPlayerEntity player) {
        if (!activePlayers.contains(player.getUuid())) return;

        activePlayers.remove(player.getUuid());
        player.changeGameMode(GameMode.SPECTATOR);
        server.getPlayerManager().broadcast(Text.literal("§c" + player.getName().getString() + " has fallen."), false);
        checkForWinner(server);
    }

    public static void checkForWinner(MinecraftServer server) {
        if (activePlayers.size() == 1) {
            UUID winnerId = activePlayers.iterator().next();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getUuid().equals(winnerId)) {
                    server.getPlayerManager().broadcast(Text.literal("§a" + player.getName().getString() + " has won the Hunger Games!"), false);
                    player.sendMessage(Text.literal("§6You are the victor!"), false);
                    break;
                }
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

    private static void buildLaunchTube(ServerWorld world, BlockPos base) {
        for (int y = 0; y <= MAX_ELEVATOR_HEIGHT + 3; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = base.add(dx, y, dz);
                    boolean edge = dx == -1 || dx == 1 || dz == -1 || dz == 1;
                    if (edge) {
                        world.setBlockState(pos, Blocks.GLASS.getDefaultState());
                    } else {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
        world.setBlockState(base, Blocks.GOLD_BLOCK.getDefaultState());
    }

    public static void onServerTick(MinecraftServer server) {
        if (gracePeriodTicks > 0) {
            gracePeriodTicks--;
            if (gracePeriodTicks == 0) {
                pvpEnabled = true;
                server.getPlayerManager().broadcast(Text.literal("§cGrace period is over! PvP is now enabled."), false);
            }
        }

        ServerWorld world = server.getOverworld();
        Iterator<Map.Entry<BlockPos, Integer>> it = elevatorProgress.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = it.next();
            BlockPos base = entry.getKey();
            int height = entry.getValue();

            if (height < MAX_ELEVATOR_HEIGHT) {
                world.setBlockState(base.up(height + 1), Blocks.GOLD_BLOCK.getDefaultState());
                world.setBlockState(base.up(height), Blocks.AIR.getDefaultState()); // Clear previous step
                entry.setValue(height + 1);
            } else {
                it.remove();
            }
        }
    }
}
