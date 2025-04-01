package net.saganetwork.herobrine;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class HerobrineChatManager {

    private static final List<String> messages = List.of(
            "Seni görüyorum...",
            "Bu dünyada yalnız değilsin.",
            "Kaçabileceğini mi sandın?",
            "Hu hu burada birisi mi var?",
            "Saklanamazsın.",
            "Sıradaki sensin."
    );

    private static final Random random = new Random();

    public static void start(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String msg = messages.get(random.nextInt(messages.size()));
                String formatted = "<Herobrine> " + msg;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(formatted);

                    Location baseLoc = player.getLocation();
                    World world = baseLoc.getWorld();

                    int offsetX = random.nextInt(11) - 5; // -5 ile +5 arası
                    int offsetZ = random.nextInt(11) - 5;

                    Location strikeLoc = baseLoc.clone().add(offsetX, 0, offsetZ);
                    strikeLoc.setY(world.getHighestBlockYAt(strikeLoc));

                    world.strikeLightningEffect(strikeLoc);
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                }

                Bukkit.getLogger().info("<Herobrine> " + msg);
            }
        }.runTaskTimer(plugin, 20L * 10, 20L * 60 * 5);
    }
}
