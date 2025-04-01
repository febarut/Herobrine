package net.saganetwork.herobrine;

import de.oliver.fancynpcs.api.FancyNpcsPlugin;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HerobrineSceneManager {

    private static boolean sceneRunning = false;
    private static final Location baseLocation = new Location(Bukkit.getWorld("world"), 0, 100, 0);
    private static final int heightOffset = 150;
    private static final Map<UUID, Location> previousLocations = new HashMap<>();
    private static final List<Block> signBlocks = new ArrayList<>();
    private static Npc herobrineNpc;

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
        World world = center.getWorld();
        if (world == null) return;

        buildGraveyard(center);

        Location base = center.clone();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                Block ground = base.clone().add(x, 0, z).getBlock();
                if (ground.getType() == Material.NETHERRACK) {
                    ground.setType(Material.GRASS_BLOCK);
                }

                Block above = base.clone().add(x, 1, z).getBlock();
                if (above.getType() == Material.FIRE) {
                    above.setType(Material.AIR);
                }
            }
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            sceneRunning = false;
            return;
        }

        teleportPlayersToGraveyard(players, center);

        Player target = players.get(players.size() / 2);

        playSpookyEffects(target);

        spawnAndMoveHerobrine(target, plugin, center);
    }

    public static void buildGraveyard(Location center) {
        World world = center.getWorld();
        signBlocks.clear();

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
                Block signBlock = signLoc.getBlock();
                signBlock.setType(Material.OAK_SIGN);

                if (signBlock.getState() instanceof Sign sign) {
                    sign.setLine(0, ChatColor.DARK_RED + "Hep buradaydım");
                    sign.setLine(1, ChatColor.DARK_RED + "Beni unuttunuz...");
                    sign.update();
                }

                signBlocks.add(signBlock);
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

    private static void playSpookyEffects(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false));

        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_STARE, 1.0f, 0.8f);
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
        data.setDisplayName("");
        herobrineNpc = FancyNpcsPlugin.get().getNpcAdapter().apply(data);
        FancyNpcsPlugin.get().getNpcManager().registerNpc(herobrineNpc);
        herobrineNpc.create();
        herobrineNpc.spawnForAll();

        new BukkitRunnable() {
            int signIndex = 0;

            @Override
            public void run() {
                Location npcLoc = herobrineNpc.getData().getLocation();
                Location targetLoc = target.getLocation();

                double dx = targetLoc.getX() - npcLoc.getX();
                double dz = targetLoc.getZ() - npcLoc.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (signIndex < signBlocks.size()) {
                    Block b = signBlocks.get(signIndex);
                    b.setType(Material.AIR);
                    b.getWorld().strikeLightningEffect(b.getLocation());
                    signIndex++;
                }

                if (signIndex == signBlocks.size()) {
                    Location base = center.clone();
                    for (int x = -5; x <= 5; x++) {
                        for (int z = -5; z <= 5; z++) {
                            Block block = base.clone().add(x, 0, z).getBlock();
                            block.setType(Material.NETHERRACK);
                        }
                    }

                    for (int x = -5; x <= 5; x++) {
                        for (int z = -5; z <= 5; z++) {
                            Block fireBlock = base.clone().add(x, 1, z).getBlock();
                            if (fireBlock.getType() == Material.AIR) {
                                fireBlock.setType(Material.FIRE);
                            }
                        }
                    }

                    target.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 1, false, false, false));

                    target.getWorld().strikeLightningEffect(target.getLocation());
                }

                if (dist < 1.3) {
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

                    herobrineNpc.removeForAll();
                    FancyNpcsPlugin.get().getNpcManager().removeNpc(herobrineNpc);
                    previousLocations.clear();
                    sceneRunning = false;
                    cancel();
                    return;
                }

                double step = 0.6;
                double moveX = dx / dist * step;
                double moveZ = dz / dist * step;
                Location newLoc = npcLoc.clone().add(moveX, 0, moveZ);
                newLoc.setYaw(getYawTowards(newLoc, targetLoc));

                herobrineNpc.getData().setLocation(newLoc);
                herobrineNpc.updateForAll();
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private static float getYawTowards(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }
}
