package net.saganetwork.herobrine;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class HerobrineSceneCommand implements CommandExecutor {

    private final Plugin plugin;

    public HerobrineSceneCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HerobrineSceneManager.triggerHerobrineScene(plugin);
        sender.sendMessage("§c[Herobrine] Sahne başlatıldı!");
        return true;
    }
}
