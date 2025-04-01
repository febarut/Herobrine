package net.saganetwork.herobrine;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Material;

import java.util.*;

public class HerobrineSceneManager {

    private static boolean sceneRunning = false;
    private static final Location baseLocation = new Location(Bukkit.getWorld("world"), 0, 100, 0);
    private static final int heightOffset = 150;
    private static final Map<UUID, Location> previousLocations = new HashMap<>();

    public static boolean isSceneRunning() {
        return sceneRunning;
    }

    public static boolean hasPreviousLocation(UUID uuid) {
        return previousLocations.containsKey(uuid);
    }

    public static Location getPreviousLocation(UUID uuid) {
        return previousLocations.get(uuid);
    }

    public static void triggerHerobrineScene(Plugin plugin) {
        if (sceneRunning) return;
        sceneRunning = true;

        Location center = baseLocation.clone().add(0, heightOffset, 0);

        if (center.getWorld() == null) {
            Bukkit.getLogger().warning("[HerobrineScene] Dünya bulunamadı!");
            return;
        }

        Bukkit.getLogger().info("[HerobrineScene] Mezarlık oluşturuluyor...");
        buildGraveyard(center);

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        if (players.isEmpty()) {
            Bukkit.getLogger().info("[HerobrineScene] Hiç oyuncu yok, sahne başlatılamaz.");
            sceneRunning = false;
            return;
        }

        Bukkit.getLogger().info("[HerobrineScene] Oyuncular mezarlığa ışınlanıyor...");
        teleportPlayersToGraveyard(players, center);

        Player target = players.get(players.size() / 2);
        Bukkit.getLogger().info("[HerobrineScene] Herobrine sahneye giriyor...");
        spawnAndMoveHerobrine(target, plugin, center);
    }

    public static void buildGraveyard(Location center) {
        World world = center.getWorld();
        int size = 10;

        for (int x = -size / 2; x < size / 2; x++) {
            for (int z = -size / 2; z < size / 2; z++) {
                Location loc = center.clone().add(x, 0, z);
                world.getBlockAt(loc).setType(Material.GRASS_BLOCK);
            }
        }

        for (int row = 0; row < 4; row++) {
            int z = -3 + row * 2;
            for (int col = 0; col < 4; col++) {
                int x = -3 + col * 2;

                Location signLoc = center.clone().add(x, 1, z);
                Block signBlock = world.getBlockAt(signLoc);
                signBlock.setType(Material.OAK_SIGN);

                org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) signBlock.getBlockData();
                signBlock.setBlockData(signData);

                if (signBlock.getState() instanceof Sign sign) {
                    sign.setLine(0, ChatColor.DARK_RED + "Hep buradaydım");
                    sign.setLine(1, ChatColor.DARK_RED + "Beni unuttunuz...");
                    sign.update();
                }
            }
        }
    }


    private static void teleportPlayersToGraveyard(List<Player> players, Location center) {
        int offset = -players.size() / 2;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            previousLocations.put(p.getUniqueId(), p.getLocation());

            Location tpLoc = center.clone().add(offset + i * 2, 1, 3);
            Location lookAt = center.clone();
            tpLoc.setDirection(lookAt.toVector().subtract(tpLoc.toVector()));

            p.teleport(tpLoc);
            p.setGameMode(GameMode.ADVENTURE);
            p.setWalkSpeed(0f);
            p.setFlySpeed(0f);
            p.setFoodLevel(10);
            p.sendTitle(ChatColor.RED + "!", ChatColor.DARK_RED + "Bir şey seni izliyor...", 10, 60, 10);
        }
    }

    private static void spawnAndMoveHerobrine(Player target, Plugin plugin, Location center) {
        String npcId = "herobrine_scene";

        Npc existing = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        if (existing != null) {
            existing.removeForAll();
            FancyNpcsPlugin.get().getNpcManager().removeNpc(existing);
        }

        Location start = center.clone().add(0, 1, -10);
        float yaw = getYawTowards(start, target.getLocation());
        start.setYaw(yaw);

        NpcData data = new NpcData(npcId, target.getUniqueId(), start);
        data.setSkin("MHF_Herobrine");
        data.setDisplayName("§cHerobrine");

        Npc npc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
        FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
        npc.create();
        npc.spawnForAll();

        new BukkitRunnable() {
            @Override
            public void run() {
                Location npcLoc = npc.getData().getLocation();
                Location targetLoc = target.getLocation();

                double dx = targetLoc.getX() - npcLoc.getX();
                double dz = targetLoc.getZ() - npcLoc.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (dist < 1.5) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        Location prev = previousLocations.get(p.getUniqueId());
                        if (prev != null) {
                            p.teleport(prev);
                            p.setGameMode(GameMode.SURVIVAL);
                            p.setWalkSpeed(0.2f);
                            p.setFlySpeed(0.1f);
                            p.sendMessage(ChatColor.GRAY + "Sanki kötü bir rüyaydı...");
                        }
                    }
                    npc.removeForAll();
                    FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
                    previousLocations.clear();
                    sceneRunning = false;
                    cancel();
                    return;
                }

                double step = 0.4;
                double moveX = dx / dist * step;
                double moveZ = dz / dist * step;
                Location newLoc = npcLoc.clone().add(moveX, 0, moveZ);
                newLoc.setYaw(getYawTowards(newLoc, targetLoc));

                npc.getData().setLocation(newLoc);
                npc.updateForAll();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private static float getYawTowards(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }
}
