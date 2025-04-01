package net.saganetwork.herobrine;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage("<Herobrine> " + msg);
                }
                Bukkit.getLogger().info("<Herobrine> " + msg);
            }
        }.runTaskTimer(plugin, 20L * 10, 20L * 120); // ilk 10s sonra her 2dk
    }
}
