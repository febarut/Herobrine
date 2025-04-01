package net.saganetwork.herobrine;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HerobrineManager {

    private static final Map<UUID, String> activeNpcs = new HashMap<>();
    private static final Random random = new Random();

    public static void spawnHerobrineNpc(Player player, Plugin plugin) {
        Location spawnLoc = getHighestNearby(player, 10);
        if (spawnLoc == null) return;

        String npcId = "herobrine_" + player.getUniqueId().toString().substring(0, 8);

        // Önceki Herobrine NPC varsa kaldır
        Npc old = FancyNpcsPlugin.get().getNpcManager().getNpc(npcId);
        if (old != null) {
            old.removeForAll();
            FancyNpcsPlugin.get().getNpcManager().removeNpc(old);
        }

        // Oyuncuya baktır
        Location targetLoc = player.getLocation();
        float yaw = getYawTowards(spawnLoc, targetLoc);
        spawnLoc.setYaw(yaw);

        // NPC oluştur
        NpcData data = new NpcData(npcId, player.getUniqueId(), spawnLoc);
        data.setSkin("MHF_Herobrine");
        data.setDisplayName(""); // İsim gizli

        Npc npc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
        FancyNpcsPlugin.get().getNpcManager().registerNpc(npc);
        npc.create();
        npc.spawnForAll();

        activeNpcs.put(player.getUniqueId(), npcId);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                Location npcLoc = npc.getData().getLocation();
                if (npcLoc.distance(player.getLocation()) <= 5) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 4, 1));
                    player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.6f);

                    npc.removeForAll();
                    FancyNpcsPlugin.get().getNpcManager().removeNpc(npc);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private static Location getHighestNearby(Player player, int radius) {
        Location base = player.getLocation();
        World world = base.getWorld();
        Location highest = null;
        int maxY = -1;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int checkX = base.getBlockX() + x;
                int checkZ = base.getBlockZ() + z;
                int y = world.getHighestBlockYAt(checkX, checkZ);
                Location loc = new Location(world, checkX + 0.5, y + 1, checkZ + 0.5);

                Material below = world.getBlockAt(loc.clone().subtract(0, 1, 0)).getType();
                if (below.isSolid() && y > maxY) {
                    maxY = y;
                    highest = loc;
                }
            }
        }

        return highest;
    }

    private static float getYawTowards(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }
}
