package net.saganetwork.herobrine;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Herobrine extends JavaPlugin {

    @Override
    public void onEnable() {

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                HerobrineManager.spawnHerobrineNpc(player, this);
            }
        }, 0L, 20L * 10 * 10); // 1 dakika

        HerobrineChatManager.start(this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            HerobrineSceneManager.triggerHerobrineScene(this);
        }, 0L, 20L * 60 * 60); // 1 saat

        getServer().getPluginManager().registerEvents(new HerobrineSceneListener(), this);

    }
}